package com.alotra.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "percentage_promotion_details")
public class PercentagePromotionDetail extends PromotionDetail {

    @Column(name = "discount_rate")
    private Integer discountRate;

    @Column(name = "max_discount_amount")
    private BigDecimal maxDiscountAmount;

    @Override
    public BigDecimal calculate(BigDecimal orderAmount) {
        BigDecimal discount = orderAmount.multiply(new BigDecimal(discountRate)).divide(new BigDecimal(100));
        if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
            return maxDiscountAmount;
        }
        return discount;
    }

    // Getters and Setters
    public Integer getDiscountRate() { return discountRate; }
    public void setDiscountRate(Integer discountRate) { this.discountRate = discountRate; }
    public BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
}
