package com.alotra.discount;

import java.math.BigDecimal;

/**
 * Strategy pattern cho các loại giảm giá.
 * Mỗi strategy biểu diễn một cách tính toán giảm giá cụ thể.
 */
public interface DiscountStrategy {
    
    /**
     * Áp dụng giảm giá (discount) cho giá cơ sở.
     * @param basePrice giá gốc
     * @return giá sau giảm giá
     */
    BigDecimal apply(BigDecimal basePrice);
    
    /**
     * Tên loại giảm giá (cho logging/debug)
     */
    String getName();
}
