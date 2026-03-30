package com.alotra.entity;

import com.alotra.entity.enums.EmployeeRole;
import com.alotra.entity.enums.EmployeeStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Employee")
@AttributeOverride(name = "id", column = @Column(name = "MaNV"))
@AttributeOverride(name = "fullName", column = @Column(name = "TenNV", nullable = false))
public class Employee extends User {

    @Enumerated(EnumType.STRING)
    @Column(name = "VaiTro", nullable = false)
    private EmployeeRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai", nullable = false)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;

    @Transient
    private String plainPassword;

    @Transient
    private String confirmPassword;

    // === Business methods ===
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

    // Getters and setters
    public EmployeeRole getRole() {
        return role;
    }

    public void setRole(EmployeeRole role) {
        this.role = role;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }

    public String getPlainPassword() {
        return plainPassword;
    }

    public void setPlainPassword(String plainPassword) {
        this.plainPassword = plainPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
