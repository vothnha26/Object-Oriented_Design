package com.alotra.service.pricing;

import com.alotra.discount.DiscountStrategy;
import java.math.BigDecimal;

/**
 * Decorator áp dụng giảm giá thông qua một Strategy.
 */
public class PromotionDecorator extends PriceDecorator {
    private final DiscountStrategy discountStrategy;

    public PromotionDecorator(PriceComponent wrapped, DiscountStrategy strategy) {
        super(wrapped);
        this.discountStrategy = strategy;
    }

    @Override
    public BigDecimal calculate() {
        BigDecimal base = wrapped.calculate();
        if (discountStrategy == null) return base;
        return discountStrategy.apply(base);
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " -> " + discountStrategy.getName();
    }
}
