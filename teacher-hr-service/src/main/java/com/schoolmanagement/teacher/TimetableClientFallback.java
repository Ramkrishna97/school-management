package com.schoolmanagement.teacher;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TimetableClientFallback implements TimetableClient {
    @Override
    public void allocateAbsences(LocalDate date) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "Timetable service unavailable; approved leave will be scanned later");
    }
}
