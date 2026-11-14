package com.schoolmanagement.timetable;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubstitutionNoticeRepository extends JpaRepository<SubstitutionNotice, Long> {
    List<SubstitutionNotice> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);
    Optional<SubstitutionNotice> findBySubstitutionId(Long substitutionId);
}
