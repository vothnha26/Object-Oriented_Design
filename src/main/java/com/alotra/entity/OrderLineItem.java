package com.alotra.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@MappedSuperclass
public abstract class OrderLineItem {

    @Column(name = "SoLuong", nullable = false)
    private Integer quantity;

    @Column(name = "DonGia", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "ThanhTien", nullable = false)
    private BigDecimal lineTotal;

    @Column(name = "GhiChu")
    private String note;

    // === Business methods ===
    public abstract BigDecimal calculateTotal();

    // Getters and setters
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
