package com.schoolmanagement.timetable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "substitution_notices")
public class SubstitutionNotice {
    public enum Status { ACTIVE, CANCELLED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private Long substitutionId;
    @Column(nullable = false)
    private Long teacherId;
    @Column(nullable = false)
    private String message;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status = Status.ACTIVE;

    protected SubstitutionNotice() {}
    public SubstitutionNotice(Substitution substitution) {
        this.substitutionId = substitution.getId();
        this.teacherId = substitution.getSubstituteTeacherId();
        TimetableEntry entry = substitution.getEntry();
        this.message = "Cover " + entry.getSubjectCode() + " for " + entry.getClassName() + "-" +
            entry.getSection() + " in period " + entry.getPeriodNumber() + " on " + substitution.getDate();
    }
    public void cancel() { status = Status.CANCELLED; }
    public Long getId() { return id; }
    public Long getSubstitutionId() { return substitutionId; }
    public Long getTeacherId() { return teacherId; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public Status getStatus() { return status; }
}
