package com.alotra.discount;

import java.math.BigDecimal;

/**
 * Chiến lược giảm giá một số tiền cố định.
 */
public class FixedAmountDiscountStrategy implements DiscountStrategy {
    private final BigDecimal discountAmount;

    public FixedAmountDiscountStrategy(BigDecimal discountAmount) {
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal apply(BigDecimal basePrice) {
        if (basePrice == null) return BigDecimal.ZERO;
        BigDecimal result = basePrice.subtract(discountAmount);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    @Override
    public String getName() {
        return "Giảm giá cố định: " + discountAmount;
    }
}
