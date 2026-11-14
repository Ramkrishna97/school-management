package com.schoolmanagement.teacher;

import com.schoolmanagement.teacher.TeacherApi.ApplyLeave;
import com.schoolmanagement.teacher.TeacherApi.Availability;
import com.schoolmanagement.teacher.TeacherApi.CreateTeacher;
import com.schoolmanagement.teacher.TeacherApi.DecideLeave;
import com.schoolmanagement.teacher.TeacherApi.DecisionResult;
import com.schoolmanagement.teacher.TeacherApi.TeacherView;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {
    private static final Logger log = LoggerFactory.getLogger(TeacherController.class);
    private final TeacherHrService service;
    private final TimetableClient timetable;

    public TeacherController(TeacherHrService service, TimetableClient timetable) {
        this.service = service;
        this.timetable = timetable;
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL')")
    public TeacherView create(@Valid @RequestBody CreateTeacher request) { return service.create(request); }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL')")
    public List<TeacherView> list() { return service.list(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL') or (hasRole('TEACHER') and authentication.tokenAttributes['teacherId'] == #p0)")
    public TeacherView get(@PathVariable Long id) { return service.get(id); }

    @GetMapping("/availability")
    @PreAuthorize("hasRole('SERVICE')")
    public List<Availability> availability(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.availability(date);
    }

    @PostMapping("/{id}/leaves") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL') or (hasRole('TEACHER') and authentication.tokenAttributes['teacherId'] == #p0)")
    public TeacherLeave applyLeave(@PathVariable Long id, @Valid @RequestBody ApplyLeave request) {
        return service.applyLeave(id, request);
    }

    @GetMapping("/{id}/leaves")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL') or (hasRole('TEACHER') and authentication.tokenAttributes['teacherId'] == #p0)")
    public List<TeacherLeave> leaves(@PathVariable Long id) { return service.leaves(id); }

    @PatchMapping("/leaves/{leaveId}/decision")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL')")
    public DecisionResult decide(@PathVariable Long leaveId, @Valid @RequestBody DecideLeave request) {
        TeacherLeave leave = service.decide(leaveId, request);
        boolean triggered = true;
        if (leave.getStatus() == TeacherLeave.Status.APPROVED) {
            for (LocalDate day = leave.getStartDate(); !day.isAfter(leave.getEndDate()); day = day.plusDays(1)) {
                try {
                    timetable.allocateAbsences(day);
                } catch (RuntimeException ex) {
                    triggered = false;
                    log.warn("Approved leave {} saved; substitution scan for {} needs retry: {}",
                        leaveId, day, ex.getMessage());
                }
            }
        }
        return new DecisionResult(leave, triggered);
    }

    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL') or (hasRole('TEACHER') and authentication.tokenAttributes['teacherId'] == #p0)")
    public TeacherCheckIn checkIn(@PathVariable Long id) { return service.checkIn(id); }

    @PostMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL') or (hasRole('TEACHER') and authentication.tokenAttributes['teacherId'] == #p0)")
    public TeacherCheckIn checkOut(@PathVariable Long id) { return service.checkOut(id); }
}
