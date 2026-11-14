package com.schoolmanagement.timetable;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TeacherClientFallback implements TeacherClient {
    @Override
    public List<TeacherAvailability> availability(LocalDate date) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "Teacher HR service unavailable; substitution allocation cannot proceed safely");
    }
}
