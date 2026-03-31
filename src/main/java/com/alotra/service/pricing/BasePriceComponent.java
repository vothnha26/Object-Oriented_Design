package com.alotra.service.pricing;

import java.math.BigDecimal;

/**
 * Base price component for starting the decorator chain.
 * Calculates base price as unitPrice * quantity.
 */
public class BasePriceComponent implements PriceComponent {
    
    private final BigDecimal unitPrice;
    private final Integer quantity;

    public BasePriceComponent(BigDecimal unitPrice, Integer quantity) {
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    @Override
    public BigDecimal calculate() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
