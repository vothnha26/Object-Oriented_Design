package com.alotra.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Chiến lược giảm giá theo phần trăm (%).
 * Ví dụ: 20% → giá = giá gốc * 80 / 100
 */
public class PercentDiscountStrategy implements DiscountStrategy {
    
    private final int percent;
    
    public PercentDiscountStrategy(int percent) {
        // Giới hạn từ 0 đến 100
        this.percent = Math.min(100, Math.max(0, percent));
    }
    
    @Override
    public BigDecimal apply(BigDecimal basePrice) {
        if (basePrice == null || percent <= 0) {
            return basePrice;
        }
        
        RoundingMode rm = RoundingMode.HALF_UP;
        // Tính multiplier: (100 - percent) / 100
        BigDecimal factor = BigDecimal.valueOf(100 - percent)
                .divide(BigDecimal.valueOf(100), 4, rm);
        
        // Áp dụng và làm tròn
        return basePrice.multiply(factor).setScale(0, rm);
    }
    
    @Override
    public String getName() {
        return "PercentDiscount(" + percent + "%)";
    }
    
    public int getPercent() {
        return percent;
    }
}
