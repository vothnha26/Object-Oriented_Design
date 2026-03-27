package com.alotra.service.pricing;

import com.alotra.entity.Topping;
import java.math.BigDecimal;
import java.util.Map;

public class ToppingDecorator extends PriceDecorator {
    private final Map<Topping, Integer> toppings;
    public ToppingDecorator(PriceComponent wrapped, Map<Topping, Integer> toppings) {
        super(wrapped);
        this.toppings = toppings;
    }
    @Override
    public BigDecimal calculate() {
        BigDecimal base = wrapped.calculate();
        if (toppings == null || toppings.isEmpty()) return base;
        
        BigDecimal extra = toppings.entrySet().stream()
                .map(e -> e.getKey().getExtraPrice().multiply(BigDecimal.valueOf(e.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return base.add(extra);
    }
    @Override
    public String getDescription() { return wrapped.getDescription() + " + Topping"; }
}
