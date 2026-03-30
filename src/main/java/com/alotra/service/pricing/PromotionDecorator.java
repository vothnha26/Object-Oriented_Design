package com.alotra.service.pricing;

import java.math.BigDecimal;

import com.alotra.discount.DiscountStrategy;

/**
 * Decorator that applies a discount strategy to a price.
 * Wraps another price component and applies discount to its calculated result.
 */
public class PromotionDecorator extends PriceDecorator {
    
    private final DiscountStrategy discountStrategy;

    public PromotionDecorator(PriceComponent delegate, DiscountStrategy discountStrategy) {
        super(delegate);
        this.discountStrategy = discountStrategy;
    }

    @Override
    public BigDecimal calculate() {
        BigDecimal price = delegate.calculate();
        return discountStrategy.apply(price);
    }
}
