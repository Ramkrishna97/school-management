package com.schoolmanagement.teacher;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teachers")
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String qualification;

    private String classTeacherOf;

    @Column(nullable = false)
    private int casualLeaveBalance = 12;

    @Column(nullable = false)
    private int sickLeaveBalance = 10;

    @Column(nullable = false)
    private int earnedLeaveBalance = 15;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "teacher_subjects", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "subject_code", nullable = false)
    private Set<String> subjectCodes = new HashSet<>();

    protected Teacher() {}

    public Teacher(String name, String department, String qualification, String classTeacherOf,
                   Set<String> subjectCodes) {
        this.name = name;
        this.department = department;
        this.qualification = qualification;
        this.classTeacherOf = classTeacherOf;
        this.subjectCodes = new HashSet<>(subjectCodes);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getQualification() { return qualification; }
    public String getClassTeacherOf() { return classTeacherOf; }
    public int getCasualLeaveBalance() { return casualLeaveBalance; }
    public int getSickLeaveBalance() { return sickLeaveBalance; }
    public int getEarnedLeaveBalance() { return earnedLeaveBalance; }
    public Set<String> getSubjectCodes() { return Set.copyOf(subjectCodes); }

    public void deductLeave(TeacherLeave.Type type, int days) {
        int remaining = switch (type) {
            case CASUAL -> casualLeaveBalance;
            case SICK -> sickLeaveBalance;
            case EARNED -> earnedLeaveBalance;
        };
        if (days > remaining) throw new IllegalArgumentException("Insufficient " + type + " leave balance");
        switch (type) {
            case CASUAL -> casualLeaveBalance -= days;
            case SICK -> sickLeaveBalance -= days;
            case EARNED -> earnedLeaveBalance -= days;
        }
    }
}
