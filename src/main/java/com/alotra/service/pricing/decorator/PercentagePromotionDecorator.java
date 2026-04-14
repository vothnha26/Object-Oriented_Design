package com.alotra.service.pricing.decorator;

import com.alotra.service.pricing.PriceComponent;
import com.alotra.service.pricing.PriceDecorator;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentagePromotionDecorator extends PriceDecorator {
    private final int percent;

    public PercentagePromotionDecorator(PriceComponent wrapped, int percent) {
        super(wrapped);
        this.percent = percent;
    }

    @Override
    public BigDecimal calculate() {
        BigDecimal base = wrapped.calculate();
        BigDecimal factor = BigDecimal.valueOf(100 - percent)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return base.multiply(factor);
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " -> Giảm " + percent + "%";
    }
}
