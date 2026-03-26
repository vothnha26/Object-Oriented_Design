package com.alotra.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "CartItem")
public class CartItem extends OrderLineItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCTGH")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaGH", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaBT", nullable = false)
    private ProductVariant variant;

    @Override
    public BigDecimal calculateTotal() {
        if (getUnitPrice() == null || getQuantity() == null) return BigDecimal.ZERO;
        return getUnitPrice().multiply(BigDecimal.valueOf(getQuantity()));
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
    public ProductVariant getVariant() { return variant; }
    public void setVariant(ProductVariant variant) { this.variant = variant; }
}
