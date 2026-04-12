package com.alotra.service.pricing;

import com.alotra.entity.OrderedTopping;
import java.math.BigDecimal;
import java.util.List;

public class ToppingDecorator extends PriceDecorator {
    private final List<OrderedTopping> toppings;

    public ToppingDecorator(PriceComponent wrapped, List<OrderedTopping> toppings) {
        super(wrapped);
        this.toppings = toppings;
    }

    @Override
    public BigDecimal calculate() {
        BigDecimal base = wrapped.calculate();
        if (toppings == null || toppings.isEmpty()) return base;
        
        BigDecimal extra = toppings.stream()
                .map(ot -> ot.getPrice().multiply(BigDecimal.valueOf(ot.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return base.add(extra);
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + Toppings";
    }
}
