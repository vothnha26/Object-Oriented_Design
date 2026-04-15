package com.alotra.entity;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(name = "username", nullable = false, unique = true)
    protected String username;

    @Column(name = "password_hash", nullable = false)
    protected String passwordHash;

    @Column(name = "email", nullable = false, unique = true)
    protected String email;

    @Column(name = "full_name")
    protected String fullName;

    @Column(name = "phone", unique = true)
    protected String phone;

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
}
