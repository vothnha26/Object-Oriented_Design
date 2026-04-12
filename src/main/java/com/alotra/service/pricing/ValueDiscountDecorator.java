package com.alotra.service.pricing;

import java.math.BigDecimal;

public class ValueDiscountDecorator extends PriceDecorator {
    private final BigDecimal discountValue;

    public ValueDiscountDecorator(PriceComponent wrapped, BigDecimal discountValue) {
        super(wrapped);
        this.discountValue = discountValue != null ? discountValue : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculate() {
        BigDecimal base = wrapped.calculate();
        BigDecimal result = base.subtract(discountValue);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " -> Giảm " + discountValue + " ₫";
    }
}
