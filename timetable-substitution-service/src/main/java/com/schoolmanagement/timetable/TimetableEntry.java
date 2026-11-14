package com.schoolmanagement.timetable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.DayOfWeek;

@Entity
@Table(name = "timetable_entries", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"day_of_week", "period_number", "class_name", "section"}),
    @UniqueConstraint(columnNames = {"day_of_week", "period_number", "teacher_id"})
})
public class TimetableEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "period_number", nullable = false)
    private int periodNumber;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(nullable = false)
    private String section;

    @Column(name = "subject_code", nullable = false)
    private String subjectCode;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    protected TimetableEntry() {}

    public TimetableEntry(DayOfWeek dayOfWeek, int periodNumber, String className, String section,
                          String subjectCode, Long teacherId) {
        this.dayOfWeek = dayOfWeek;
        this.periodNumber = periodNumber;
        this.className = className;
        this.section = section;
        this.subjectCode = subjectCode;
        this.teacherId = teacherId;
    }

    public Long getId() { return id; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public int getPeriodNumber() { return periodNumber; }
    public String getClassName() { return className; }
    public String getSection() { return section; }
    public String getSubjectCode() { return subjectCode; }
    public Long getTeacherId() { return teacherId; }
}
