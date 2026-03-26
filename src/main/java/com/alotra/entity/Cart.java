package com.alotra.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "Cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGH")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaKH", nullable = false)
    private Customer customer;

    @Column(name = "TrangThai", nullable = false)
    private String status; // ACTIVE, CHECKED_OUT, CANCELLED

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<CartItem> items = new java.util.ArrayList<>();

    // === Business methods ===
    public int getItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItem::calculateTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @PrePersist
    public void prePersist() {
        if (status == null || status.isBlank()) status = "ACTIVE";
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public java.util.List<CartItem> getItems() { return items; }
    public void setItems(java.util.List<CartItem> items) { this.items = items; }
}
