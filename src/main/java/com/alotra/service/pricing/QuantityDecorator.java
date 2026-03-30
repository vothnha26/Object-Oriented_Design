package com.alotra.service.pricing;

import java.math.BigDecimal;

public class QuantityDecorator extends PriceDecorator {
    private final int quantity;
    public QuantityDecorator(PriceComponent wrapped, int quantity) {
        super(wrapped);
        this.quantity = quantity;
    }
    @Override
    public BigDecimal calculate() {
        return wrapped.calculate().multiply(BigDecimal.valueOf(quantity));
    }
    @Override
    public String getDescription() { return wrapped.getDescription() + " x " + quantity; }
}
