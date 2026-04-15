package com.alotra.entity;

import com.alotra.entity.enums.CustomerStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class Customer extends User {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Override
    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }

    @Override
    public String getDisplayName() {
        return fullName;
    }

    // Getters and Setters
    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }
}
