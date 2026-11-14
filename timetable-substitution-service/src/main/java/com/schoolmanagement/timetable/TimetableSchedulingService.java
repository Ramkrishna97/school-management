package com.schoolmanagement.timetable;

import com.schoolmanagement.timetable.TimetableApi.CreateEntry;
import com.schoolmanagement.timetable.TimetableApi.DailyPeriod;
import com.schoolmanagement.timetable.TimetableApi.EntryView;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TimetableSchedulingService {
    private final TimetableRepository timetables;
    private final SubstitutionRepository substitutions;
    private final TimetablePolicy policy;
    private final TeacherClient teachers;

    public TimetableSchedulingService(TimetableRepository timetables, SubstitutionRepository substitutions,
                                      TimetablePolicy policy, TeacherClient teachers) {
        this.timetables = timetables;
        this.substitutions = substitutions;
        this.policy = policy;
        this.teachers = teachers;
    }

    @Transactional
    public EntryView create(CreateEntry request) {
        if (request.periodNumber() > policy.periodsPerDay()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Period exceeds the school day");
        }
        String subject = request.subjectCode().trim().toUpperCase(Locale.ROOT);
        boolean qualified = teachers.availability(LocalDate.now()).stream()
            .anyMatch(t -> t.id().equals(request.teacherId()) && t.subjectCodes().contains(subject));
        if (!qualified) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teacher is not qualified for the subject");
        }
        String className = request.className().trim();
        String section = request.section().trim();
        if (timetables.existsByDayOfWeekAndPeriodNumberAndClassNameAndSection(request.dayOfWeek(),
                request.periodNumber(), className, section) ||
            timetables.existsByDayOfWeekAndPeriodNumberAndTeacherId(request.dayOfWeek(),
                request.periodNumber(), request.teacherId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Class or teacher already scheduled for this period");
        }
        Set<Integer> occupied = timetables.findByDayOfWeekOrderByPeriodNumberAscIdAsc(request.dayOfWeek())
            .stream().filter(entry -> entry.getTeacherId().equals(request.teacherId()))
            .map(TimetableEntry::getPeriodNumber).collect(Collectors.toSet());
        if (!policy.canAddPeriod(occupied, request.periodNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teacher workload or break policy would be exceeded");
        }
        return EntryView.from(timetables.save(new TimetableEntry(request.dayOfWeek(),
            request.periodNumber(), className, section, subject, request.teacherId())));
    }

    @Transactional(readOnly = true)
    public List<EntryView> weekly(DayOfWeek day) {
        return timetables.findByDayOfWeekOrderByPeriodNumberAscIdAsc(day)
            .stream().map(EntryView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DailyPeriod> daily(LocalDate date, String className, String section) {
        Map<Integer, TimetableEntry> entries = timetables
            .findByDayOfWeekOrderByPeriodNumberAscIdAsc(date.getDayOfWeek()).stream()
            .filter(entry -> entry.getClassName().equalsIgnoreCase(className) &&
                entry.getSection().equalsIgnoreCase(section))
            .collect(Collectors.toMap(TimetableEntry::getPeriodNumber, entry -> entry));
        Map<Long, Substitution> assigned = substitutions.findByDate(date).stream()
            .collect(Collectors.toMap(sub -> sub.getEntry().getId(), sub -> sub));
        // Resolve the effective teacher from today's leave and substitution state.
        Set<Long> absent = teachers.availability(date).stream().filter(TeacherClient.TeacherAvailability::absent)
            .map(TeacherClient.TeacherAvailability::id).collect(Collectors.toSet());
        List<DailyPeriod> result = new ArrayList<>();
        for (int period = 1; period <= policy.periodsPerDay(); period++) {
            TimetableEntry entry = entries.get(period);
            if (entry == null) {
                result.add(new DailyPeriod(date, period, className, section, null, null, null, "FREE"));
                continue;
            }
            Substitution substitute = assigned.get(entry.getId());
            Long effective = substitute != null ? substitute.getSubstituteTeacherId() :
                absent.contains(entry.getTeacherId()) ? null : entry.getTeacherId();
            String status = substitute != null ? "SUBSTITUTED" :
                effective == null ? "UNFILLED" : "REGULAR";
            result.add(new DailyPeriod(date, period, className, section,
                entry.getSubjectCode(), entry.getTeacherId(), effective, status));
        }
        return result;
    }
}
