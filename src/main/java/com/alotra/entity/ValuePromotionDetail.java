package com.alotra.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "value_promotion_details")
public class ValuePromotionDetail extends PromotionDetail {

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @Override
    public BigDecimal calculate(BigDecimal orderAmount) {
        return discountValue;
    }

    // Getters and Setters
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
}
