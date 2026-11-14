package com.schoolmanagement.timetable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

@Entity
@Table(name = "substitutions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"substitution_date", "timetable_entry_id"}),
    @UniqueConstraint(columnNames = {"substitution_date", "period_number", "substitute_teacher_id"})
})
public class Substitution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "substitution_date", nullable = false)
    private LocalDate date;

    @ManyToOne(optional = false)
    @JoinColumn(name = "timetable_entry_id", nullable = false)
    private TimetableEntry entry;

    @Column(name = "substitute_teacher_id", nullable = false)
    private Long substituteTeacherId;

    @Column(name = "period_number", nullable = false)
    private int periodNumber;

    protected Substitution() {}

    public Substitution(LocalDate date, TimetableEntry entry, Long substituteTeacherId) {
        this.date = date;
        this.entry = entry;
        this.substituteTeacherId = substituteTeacherId;
        this.periodNumber = entry.getPeriodNumber();
    }

    public Long getId() { return id; }
    public LocalDate getDate() { return date; }
    public TimetableEntry getEntry() { return entry; }
    public Long getSubstituteTeacherId() { return substituteTeacherId; }
    public int getPeriodNumber() { return periodNumber; }
}
