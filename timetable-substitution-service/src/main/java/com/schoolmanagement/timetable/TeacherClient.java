package com.schoolmanagement.timetable;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "teacher-hr-service", fallback = TeacherClientFallback.class)
public interface TeacherClient {
    @GetMapping("/api/teachers/availability")
    List<TeacherAvailability> availability(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);

    record TeacherAvailability(Long id, String name, String department,
                               Set<String> subjectCodes, boolean absent) {}
}
