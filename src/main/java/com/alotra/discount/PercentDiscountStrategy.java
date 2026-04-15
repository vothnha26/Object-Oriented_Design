package com.alotra.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Chiến lược giảm giá theo phần trăm (%), có giới hạn số tiền giảm tối đa.
 */
public class PercentDiscountStrategy implements DiscountStrategy {

    private final int percent;
    private final BigDecimal maxDiscount;

    public PercentDiscountStrategy(int percent) {
        this(percent, null);
    }

    public PercentDiscountStrategy(int percent, BigDecimal maxDiscount) {
        this.percent = Math.min(100, Math.max(0, percent));
        this.maxDiscount = maxDiscount;
    }

    @Override
    public BigDecimal apply(BigDecimal basePrice) {
        if (basePrice == null || percent <= 0) {
            return basePrice;
        }

        BigDecimal discount = basePrice.multiply(new BigDecimal(percent))
                                      .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        
        if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
            discount = maxDiscount;
        }

        BigDecimal result = basePrice.subtract(discount);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    @Override
    public String getName() {
        return "Giảm giá " + percent + "%" + (maxDiscount != null ? " (Tối đa " + maxDiscount + ")" : "");
    }
}
