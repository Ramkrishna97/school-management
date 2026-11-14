package com.schoolmanagement.teacher;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "teacher_check_ins", uniqueConstraints =
    @UniqueConstraint(columnNames = {"teacher_id", "work_date"}))
public class TeacherCheckIn {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;
    @Column(nullable = false)
    private Instant checkInAt;
    private Instant checkOutAt;

    protected TeacherCheckIn() {}
    public TeacherCheckIn(Long teacherId, LocalDate workDate) {
        this.teacherId = teacherId;
        this.workDate = workDate;
        this.checkInAt = Instant.now();
    }
    public void checkOut() {
        if (checkOutAt != null) throw new IllegalArgumentException("Already checked out");
        checkOutAt = Instant.now();
    }
    public Long getId() { return id; }
    public Long getTeacherId() { return teacherId; }
    public LocalDate getWorkDate() { return workDate; }
    public Instant getCheckInAt() { return checkInAt; }
    public Instant getCheckOutAt() { return checkOutAt; }
}
