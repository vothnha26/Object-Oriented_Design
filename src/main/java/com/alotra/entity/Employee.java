package com.alotra.entity;

import com.alotra.entity.enums.EmployeeRole;
import com.alotra.entity.enums.EmployeeStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "employees")
public class Employee extends User {

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private EmployeeRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Transient
    private String plainPassword;

    @Transient
    private String confirmPassword;

    public boolean isAdmin() {
        return role == EmployeeRole.ADMIN;
    }

    @Override
    public boolean isActive() {
        return status == EmployeeStatus.ACTIVE;
    }

    @Override
    public String getDisplayName() {
        return fullName;
    }

    // Getters and Setters
    public EmployeeRole getRole() { return role; }
    public void setRole(EmployeeRole role) { this.role = role; }
    public EmployeeStatus getStatus() { return status; }
    public void setStatus(EmployeeStatus status) { this.status = status; }
    public String getPlainPassword() { return plainPassword; }
    public void setPlainPassword(String plainPassword) { this.plainPassword = plainPassword; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
