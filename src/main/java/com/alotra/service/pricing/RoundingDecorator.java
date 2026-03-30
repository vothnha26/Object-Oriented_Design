package com.alotra.service.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Decorator that rounds the final price to a whole number.
 * Ensures final price is always rounded to scale 0 using HALF_UP mode.
 */
public class RoundingDecorator extends PriceDecorator {

    public RoundingDecorator(PriceComponent delegate) {
        super(delegate);
    }

    @Override
    public BigDecimal calculate() {
        BigDecimal price = delegate.calculate();
        return price.setScale(0, RoundingMode.HALF_UP);
    }
}
