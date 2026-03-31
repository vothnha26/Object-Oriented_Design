package com.alotra.discount;

import java.math.BigDecimal;

/**
 * Chiến lược không áp dụng giảm giá (trả về giá gốc).
 */
public class NoDiscountStrategy implements DiscountStrategy {
    
    @Override
    public BigDecimal apply(BigDecimal basePrice) {
        return basePrice;
    }
    
    @Override
    public String getName() {
        return "NoDiscount";
    }
}
