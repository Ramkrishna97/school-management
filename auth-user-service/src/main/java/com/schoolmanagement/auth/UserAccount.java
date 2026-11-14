package com.schoolmanagement.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_accounts")
public class UserAccount {
    public enum Role { SUPER_ADMIN, PRINCIPAL, TEACHER, ACCOUNTANT, LIBRARIAN,
                       STORE_MANAGER, STUDENT, PARENT }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Role role;
    private Long teacherId;
    private Long studentId;
    @Column(nullable = false)
    private boolean enabled = true;

    protected UserAccount() {}
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public Long getTeacherId() { return teacherId; }
    public Long getStudentId() { return studentId; }
    public boolean isEnabled() { return enabled; }
}
