package com.alotra.service.pricing;

import java.math.BigDecimal;

/**
 * Lớp cơ sở chứa giá gốc (Subtotal).
 */
public class BasePrice implements PriceComponent {
    private final BigDecimal amount;

    public BasePrice(BigDecimal amount) {
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculate() {
        return amount;
    }

    @Override
    public String getDescription() {
        return "Giá gốc: " + amount;
    }
}
