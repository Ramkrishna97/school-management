package com.schoolmanagement.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "parent_student_links", uniqueConstraints =
    @UniqueConstraint(columnNames = {"parent_user_id", "student_id"}))
public class ParentStudentLink {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long parentUserId;
    @Column(nullable = false)
    private Long studentId;

    protected ParentStudentLink() {}
    public ParentStudentLink(Long parentUserId, Long studentId) {
        this.parentUserId = parentUserId;
        this.studentId = studentId;
    }
    public Long getId() { return id; }
    public Long getParentUserId() { return parentUserId; }
    public Long getStudentId() { return studentId; }
}
