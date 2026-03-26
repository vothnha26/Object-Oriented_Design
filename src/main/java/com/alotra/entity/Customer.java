package com.alotra.entity;

import com.alotra.entity.enums.CustomerStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "Customer")
@AttributeOverride(name = "id", column = @Column(name = "MaKH"))
@AttributeOverride(name = "fullName", column = @Column(name = "TenKH", nullable = false))
public class Customer extends User {

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai", nullable = false)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Override
    public boolean isActive() { return status == CustomerStatus.ACTIVE; }

    @Override
    public String getDisplayName() { return fullName; }

    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }
}
