package com.schoolmanagement.teacher;

import com.schoolmanagement.teacher.TeacherApi.ApplyLeave;
import com.schoolmanagement.teacher.TeacherApi.Availability;
import com.schoolmanagement.teacher.TeacherApi.CreateTeacher;
import com.schoolmanagement.teacher.TeacherApi.TeacherView;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TeacherHrService {
    private final TeacherRepository teachers;
    private final TeacherLeaveRepository leaves;
    private final TeacherCheckInRepository checkIns;

    public TeacherHrService(TeacherRepository teachers, TeacherLeaveRepository leaves,
                            TeacherCheckInRepository checkIns) {
        this.teachers = teachers;
        this.leaves = leaves;
        this.checkIns = checkIns;
    }

    @Transactional
    public TeacherView create(CreateTeacher request) {
        Set<String> subjects = request.subjectCodes().stream()
            .map(code -> code.trim().toUpperCase(Locale.ROOT)).collect(Collectors.toSet());
        return TeacherView.from(teachers.save(new Teacher(request.name().trim(),
            request.department().trim(), request.qualification().trim(),
            request.classTeacherOf(), subjects)));
    }

    @Transactional(readOnly = true)
    public List<TeacherView> list() { return teachers.findAll().stream().map(TeacherView::from).toList(); }

    @Transactional(readOnly = true)
    public TeacherView get(Long id) { return TeacherView.from(findTeacher(id)); }

    @Transactional(readOnly = true)
    public List<Availability> availability(LocalDate date) {
        Set<Long> absentIds = leaves.approvedOn(date, TeacherLeave.Status.APPROVED)
            .stream().map(TeacherLeave::getTeacherId).collect(Collectors.toSet());
        return teachers.findAll().stream()
            .map(t -> new Availability(t.getId(), t.getName(), t.getDepartment(),
                t.getSubjectCodes(), absentIds.contains(t.getId()))).toList();
    }

    @Transactional
    public TeacherLeave applyLeave(Long teacherId, ApplyLeave request) {
        findTeacher(teacherId);
        if (request.endDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date precedes start date");
        }
        if (leaves.overlaps(teacherId, request.startDate(), request.endDate(),
            List.of(TeacherLeave.Status.PENDING, TeacherLeave.Status.APPROVED))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Leave dates overlap an existing request");
        }
        return leaves.save(new TeacherLeave(teacherId, request.startDate(), request.endDate(),
            request.type(), request.reason().trim()));
    }

    @Transactional(readOnly = true)
    public List<TeacherLeave> leaves(Long teacherId) {
        findTeacher(teacherId);
        return leaves.findByTeacherIdOrderByStartDateDesc(teacherId);
    }

    @Transactional
    public TeacherLeave decide(Long leaveId, TeacherApi.DecideLeave decision) {
        TeacherLeave leave = leaves.findById(leaveId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found"));
        if (decision.status() == TeacherLeave.Status.APPROVED) {
            findTeacher(leave.getTeacherId()).deductLeave(leave.getType(), leave.days());
        }
        leave.decide(decision.status(), decision.note());
        return leave;
    }

    @Transactional
    public TeacherCheckIn checkIn(Long teacherId) {
        findTeacher(teacherId);
        LocalDate today = LocalDate.now();
        if (checkIns.findByTeacherIdAndWorkDate(teacherId, today).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already checked in today");
        }
        return checkIns.save(new TeacherCheckIn(teacherId, today));
    }

    @Transactional
    public TeacherCheckIn checkOut(Long teacherId) {
        TeacherCheckIn record = checkIns.findByTeacherIdAndWorkDate(teacherId, LocalDate.now())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Not checked in today"));
        record.checkOut();
        return record;
    }

    private Teacher findTeacher(Long id) {
        return teachers.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));
    }
}
