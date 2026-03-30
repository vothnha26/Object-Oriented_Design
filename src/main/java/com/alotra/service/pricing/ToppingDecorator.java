package com.alotra.service.pricing;

import java.math.BigDecimal;
import java.util.Collection;

import com.alotra.entity.SelectedTopping;

/**
 * Decorator that adds topping costs on top of a base price.
 * Calculates total topping cost and adds it to the wrapped component's price.
 */
public class ToppingDecorator extends PriceDecorator {
    
    private final Collection<SelectedTopping> toppings;

    public ToppingDecorator(PriceComponent delegate, Collection<SelectedTopping> toppings) {
        super(delegate);
        this.toppings = toppings;
    }

    @Override
    public BigDecimal calculate() {
        BigDecimal basePrice = delegate.calculate();
        BigDecimal toppingTotal = toppings != null ? 
            toppings.stream()
                .map(SelectedTopping::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add) :
            BigDecimal.ZERO;
        return basePrice.add(toppingTotal);
    }
}
