package com.alotra.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "promotion_details")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class PromotionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "min_order_amount")
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
}
