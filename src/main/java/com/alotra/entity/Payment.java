package com.alotra.entity;

import com.alotra.entity.enums.PaymentMethod;
import com.alotra.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "Method", nullable = false)
    private PaymentMethod method = PaymentMethod.CASH;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private PaymentStatus status = PaymentStatus.UNPAID;

    @Column(name = "Amount")
    private BigDecimal amount;

    @Column(name = "PaidAt")
    private LocalDateTime paidAt;

    @Column(name = "TransactionRef")
    private String transactionRef;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
}
