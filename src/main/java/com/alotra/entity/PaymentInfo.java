package com.alotra.entity;

import com.alotra.entity.enums.PaymentMethod;
import com.alotra.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Embeddable
public class PaymentInfo {

    @Enumerated(EnumType.STRING)
    @Column(name = "PaymentStatus", nullable = false)
    private PaymentStatus status = PaymentStatus.UNPAID;

    @Enumerated(EnumType.STRING)
    @Column(name = "PaymentMethod")
    private PaymentMethod method;

    @Column(name = "PaidAt")
    private LocalDateTime paidAt;

    public boolean isPaid() {
        return status == PaymentStatus.PAID;
    }

    // Getters and Setters
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
