package com.schoolmanagement.teacher;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeacherLeaveRepository extends JpaRepository<TeacherLeave, Long> {
    @Query("select l from TeacherLeave l where l.startDate <= :date and l.endDate >= :date and l.status = :status")
    List<TeacherLeave> approvedOn(@Param("date") LocalDate date,
                                  @Param("status") TeacherLeave.Status status);

    @Query("select count(l) > 0 from TeacherLeave l where l.teacherId = :teacherId " +
        "and l.status in :statuses and l.startDate <= :endDate and l.endDate >= :startDate")
    boolean overlaps(@Param("teacherId") Long teacherId, @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate,
                     @Param("statuses") Collection<TeacherLeave.Status> statuses);

    List<TeacherLeave> findByTeacherIdOrderByStartDateDesc(Long teacherId);
}
