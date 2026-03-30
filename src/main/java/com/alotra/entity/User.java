package com.alotra.entity;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(name = "Username", nullable = false, unique = true)
    protected String username;

    @Column(name = "MatKhauHash", nullable = false)
    protected String passwordHash;

    @Column(name = "Email", nullable = false, unique = true)
    protected String email;

    protected String fullName;

    @Column(name = "SoDienThoai", unique = true)
    protected String phone;

    @Column(name = "NgayTao", nullable = false, updatable = false)
    protected java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    public abstract String getDisplayName();
    public abstract boolean isActive();

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
}
