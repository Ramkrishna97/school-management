package com.schoolmanagement.teacher;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "teacher_leaves")
public class TeacherLeave {
    public enum Type { CASUAL, SICK, EARNED }
    public enum Status { PENDING, APPROVED, REJECTED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long teacherId;
    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Type type;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status = Status.PENDING;
    @Column(nullable = false)
    private String reason;
    private String decisionNote;

    protected TeacherLeave() {}
    public TeacherLeave(Long teacherId, LocalDate startDate, LocalDate endDate, Type type, String reason) {
        this.teacherId = teacherId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.reason = reason;
    }

    public void decide(Status decision, String note) {
        if (status != Status.PENDING || decision == Status.PENDING) {
            throw new IllegalArgumentException("Only pending leave can be approved or rejected");
        }
        status = decision;
        decisionNote = note;
    }

    public int days() { return Math.toIntExact(ChronoUnit.DAYS.between(startDate, endDate) + 1); }
    public Long getId() { return id; }
    public Long getTeacherId() { return teacherId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Type getType() { return type; }
    public Status getStatus() { return status; }
    public String getReason() { return reason; }
    public String getDecisionNote() { return decisionNote; }
}
