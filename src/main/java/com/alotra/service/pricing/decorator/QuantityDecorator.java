package com.alotra.service.pricing.decorator;

import com.alotra.service.pricing.PriceComponent;
import com.alotra.service.pricing.PriceDecorator;
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
