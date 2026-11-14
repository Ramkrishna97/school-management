package com.schoolmanagement.timetable;

import com.schoolmanagement.timetable.TeacherClient.TeacherAvailability;
import com.schoolmanagement.timetable.TimetableApi.Allocation;
import com.schoolmanagement.timetable.TimetableApi.Workload;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubstitutionService {
    private final TimetableRepository timetables;
    private final SubstitutionRepository substitutions;
    private final SubstitutionNoticeRepository notices;
    private final TeacherClient teacherClient;
    private final TimetablePolicy policy;

    public SubstitutionService(TimetableRepository timetables, SubstitutionRepository substitutions,
                               SubstitutionNoticeRepository notices,
                               TeacherClient teacherClient, TimetablePolicy policy) {
        this.timetables = timetables;
        this.substitutions = substitutions;
        this.notices = notices;
        this.teacherClient = teacherClient;
        this.policy = policy;
    }

    @Scheduled(cron = "${school.timetable.absence-scan-cron}")
    public void scanToday() {
        allocate(LocalDate.now());
    }

    @Transactional
    public synchronized List<Allocation> allocate(LocalDate date) {
        // Serialize scans in this process; database uniqueness also rejects same-slot duplicates.
        List<TeacherAvailability> teachers = teacherClient.availability(date);
        Set<Long> absentIds = teachers.stream().filter(TeacherAvailability::absent)
            .map(TeacherAvailability::id).collect(Collectors.toSet());
        List<TimetableEntry> entries = timetables.findByDayOfWeekOrderByPeriodNumberAscIdAsc(date.getDayOfWeek());
        List<Substitution> existing = substitutions.findByDate(date);
        // A substitute can subsequently take leave. Release those assignments for reallocation.
        List<Substitution> invalid = existing.stream()
            .filter(s -> absentIds.contains(s.getSubstituteTeacherId())).toList();
        if (!invalid.isEmpty()) {
            for (Substitution invalidAssignment : invalid) {
                notices.findBySubstitutionId(invalidAssignment.getId()).ifPresent(SubstitutionNotice::cancel);
            }
            substitutions.deleteAll(invalid);
            substitutions.flush();
        }
        existing = existing.stream().filter(s -> !absentIds.contains(s.getSubstituteTeacherId())).toList();
        Map<Long, Substitution> assignedByEntry = existing.stream()
            .collect(Collectors.toMap(s -> s.getEntry().getId(), s -> s));

        Map<Long, Set<Integer>> occupied = new HashMap<>();
        // An absent teacher's regular periods do not count as occupied for that day.
        for (TimetableEntry entry : entries) {
            if (!absentIds.contains(entry.getTeacherId())) {
                occupied.computeIfAbsent(entry.getTeacherId(), ignored -> new HashSet<>())
                    .add(entry.getPeriodNumber());
            }
        }
        for (Substitution substitution : existing) {
            occupied.computeIfAbsent(substitution.getSubstituteTeacherId(), ignored -> new HashSet<>())
                .add(substitution.getEntry().getPeriodNumber());
        }

        List<Allocation> results = new ArrayList<>();
        for (TimetableEntry entry : entries) {
            if (!absentIds.contains(entry.getTeacherId())) continue;
            if (assignedByEntry.containsKey(entry.getId())) {
                results.add(Allocation.assigned(assignedByEntry.get(entry.getId())));
                continue;
            }
            TeacherAvailability chosen = teachers.stream()
                .filter(t -> !t.absent() && !t.id().equals(entry.getTeacherId()))
                .filter(t -> t.subjectCodes() != null && t.subjectCodes().contains(entry.getSubjectCode()))
                .filter(t -> policy.canAddPeriod(occupied.getOrDefault(t.id(), Set.of()), entry.getPeriodNumber()))
                .min(Comparator.comparingInt((TeacherAvailability t) ->
                    occupied.getOrDefault(t.id(), Set.of()).size()).thenComparing(TeacherAvailability::id))
                .orElse(null);
            if (chosen == null) {
                results.add(Allocation.unfilled(entry));
                continue;
            }
            Substitution assigned = substitutions.save(new Substitution(date, entry, chosen.id()));
            substitutions.flush();
            notices.save(new SubstitutionNotice(assigned));
            occupied.computeIfAbsent(chosen.id(), ignored -> new HashSet<>()).add(entry.getPeriodNumber());
            results.add(Allocation.assigned(assigned));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public Workload workload(Long teacherId, LocalDate date) {
        Set<Integer> regular = timetables.findByDayOfWeekOrderByPeriodNumberAscIdAsc(date.getDayOfWeek())
            .stream().filter(entry -> entry.getTeacherId().equals(teacherId))
            .map(TimetableEntry::getPeriodNumber).collect(Collectors.toSet());
        Set<Integer> extra = substitutions.findByDate(date).stream()
            .filter(s -> s.getSubstituteTeacherId().equals(teacherId))
            .map(s -> s.getEntry().getPeriodNumber()).collect(Collectors.toSet());
        Set<Integer> all = new HashSet<>(regular);
        all.addAll(extra);
        return new Workload(teacherId, date, regular.size(), extra.size(), all.size(),
            policy.periodsPerDay() - all.size());
    }

    @Transactional(readOnly = true)
    public List<Allocation> assignments(LocalDate date) {
        return substitutions.findByDate(date).stream().map(Allocation::assigned).toList();
    }

    @Transactional(readOnly = true)
    public List<SubstitutionNotice> noticesForTeacher(Long teacherId) {
        return notices.findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }
}
