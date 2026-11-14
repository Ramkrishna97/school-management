package com.schoolmanagement.teacher;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherCheckInRepository extends JpaRepository<TeacherCheckIn, Long> {
    Optional<TeacherCheckIn> findByTeacherIdAndWorkDate(Long teacherId, LocalDate workDate);
}
