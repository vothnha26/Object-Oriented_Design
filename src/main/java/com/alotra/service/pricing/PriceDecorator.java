package com.alotra.service.pricing;

import java.math.BigDecimal;

/**
 * Abstract decorator for price components.
 * Allows wrapping one price component with another to build complex pricing logic.
 */
public abstract class PriceDecorator implements PriceComponent {
    
    protected final PriceComponent delegate;

    public PriceDecorator(PriceComponent delegate) {
        this.delegate = delegate;
    }

    @Override
    public BigDecimal calculate() {
        return delegate.calculate();
    }
}
