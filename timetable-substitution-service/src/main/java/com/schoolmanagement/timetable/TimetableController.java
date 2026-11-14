package com.schoolmanagement.timetable;

import com.schoolmanagement.timetable.TimetableApi.Allocation;
import com.schoolmanagement.timetable.TimetableApi.CreateEntry;
import com.schoolmanagement.timetable.TimetableApi.DailyPeriod;
import com.schoolmanagement.timetable.TimetableApi.EntryView;
import com.schoolmanagement.timetable.TimetableApi.Workload;
import jakarta.validation.Valid;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimetableController {
    private final TimetableSchedulingService scheduling;
    private final SubstitutionService allocations;

    public TimetableController(TimetableSchedulingService scheduling, SubstitutionService allocations) {
        this.scheduling = scheduling;
        this.allocations = allocations;
    }

    @PostMapping("/api/timetables")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL')")
    public EntryView create(@Valid @RequestBody CreateEntry request) {
        return scheduling.create(request);
    }

    @GetMapping("/api/timetables")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL','TEACHER')")
    public List<EntryView> list(@RequestParam DayOfWeek dayOfWeek) {
        return scheduling.weekly(dayOfWeek);
    }

    @GetMapping("/api/timetables/daily")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL','TEACHER')")
    public List<DailyPeriod> daily(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestParam String className, @RequestParam String section) {
        return scheduling.daily(date, className, section);
    }

    @GetMapping("/api/timetables/workload/{teacherId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL','TEACHER')")
    public Workload workload(@PathVariable Long teacherId,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return allocations.workload(teacherId, date);
    }

    @PostMapping("/api/substitutions/allocate-absences/{date}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL','SERVICE')")
    public List<Allocation> allocate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return allocations.allocate(date);
    }

    @GetMapping("/api/substitutions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL','TEACHER')")
    public List<Allocation> assignments(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return allocations.assignments(date);
    }

    @GetMapping("/api/substitutions/notices/mine")
    @PreAuthorize("hasRole('TEACHER')")
    public List<SubstitutionNotice> myNotices(@AuthenticationPrincipal Jwt jwt) {
        Number teacherId = jwt.getClaim("teacherId");
        if (teacherId == null) return List.of();
        return allocations.noticesForTeacher(teacherId.longValue());
    }

    @GetMapping("/api/substitutions/notices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL')")
    public List<SubstitutionNotice> notices(@RequestParam Long teacherId) {
        return allocations.noticesForTeacher(teacherId);
    }
}
