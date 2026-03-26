package com.alotra.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "OrderItem")
public class OrderItem extends OrderLineItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCT")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDH", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaBT", nullable = false)
    private ProductVariant variant;

    @Column(name = "GiamGiaDong", nullable = false)
    private BigDecimal lineDiscount = BigDecimal.ZERO;

    @Override
    public BigDecimal calculateTotal() {
        if (getUnitPrice() == null || getQuantity() == null) return BigDecimal.ZERO;
        BigDecimal raw = getUnitPrice().multiply(BigDecimal.valueOf(getQuantity()));
        return raw.subtract(lineDiscount != null ? lineDiscount : BigDecimal.ZERO);
    }

    public BigDecimal getNetTotal() { return calculateTotal(); }
    public BigDecimal getLineTotal() { return calculateTotal(); }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public ProductVariant getVariant() { return variant; }
    public void setVariant(ProductVariant variant) { this.variant = variant; }
    public BigDecimal getLineDiscount() { return lineDiscount; }
    public void setLineDiscount(BigDecimal lineDiscount) { this.lineDiscount = lineDiscount; }
}
