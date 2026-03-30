package com.alotra.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "OrderedTopping")
public class OrderedTopping {
    @EmbeddedId
    private OrderedToppingId id = new OrderedToppingId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderItemId")
    @JoinColumn(name = "MaCT")
    private OrderItem orderLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("toppingId")
    @JoinColumn(name = "MaTopping")
    private Topping topping;

    @Column(name = "SoLuong", nullable = false)
    private Integer quantity = 1;

    @Column(name = "DonGia", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "ThanhTien", nullable = false)
    private BigDecimal lineTotal;

    public OrderedToppingId getId() { return id; }
    public void setId(OrderedToppingId id) { this.id = id; }
    public OrderItem getOrderLine() { return orderLine; }
    public void setOrderLine(OrderItem orderLine) { this.orderLine = orderLine; }
    public Topping getTopping() { return topping; }
    public void setTopping(Topping topping) { this.topping = topping; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
