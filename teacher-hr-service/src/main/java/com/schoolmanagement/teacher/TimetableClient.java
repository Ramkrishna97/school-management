package com.schoolmanagement.teacher;

import java.time.LocalDate;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "timetable-substitution-service", fallback = TimetableClientFallback.class)
public interface TimetableClient {
    @PostMapping("/api/substitutions/allocate-absences/{date}")
    void allocateAbsences(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
}
