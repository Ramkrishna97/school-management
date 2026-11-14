package com.schoolmanagement.timetable;

import java.time.DayOfWeek;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimetableRepository extends JpaRepository<TimetableEntry, Long> {
    List<TimetableEntry> findByDayOfWeekOrderByPeriodNumberAscIdAsc(DayOfWeek dayOfWeek);
    boolean existsByDayOfWeekAndPeriodNumberAndClassNameAndSection(
        DayOfWeek dayOfWeek, int periodNumber, String className, String section);
    boolean existsByDayOfWeekAndPeriodNumberAndTeacherId(
        DayOfWeek dayOfWeek, int periodNumber, Long teacherId);
}
