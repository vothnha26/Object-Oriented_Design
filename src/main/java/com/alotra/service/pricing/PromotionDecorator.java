package com.alotra.service.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PromotionDecorator extends PriceDecorator {
    private final int percent;
    public PromotionDecorator(PriceComponent wrapped, int percent) {
        super(wrapped);
        this.percent = percent;
    }
    @Override
    public BigDecimal calculate() {
        BigDecimal base = wrapped.calculate();
        BigDecimal factor = BigDecimal.valueOf(100 - percent)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return base.multiply(factor).setScale(0, RoundingMode.HALF_UP);
    }
    @Override
    public String getDescription() { return wrapped.getDescription() + " -> Giảm " + percent + "%"; }
}
