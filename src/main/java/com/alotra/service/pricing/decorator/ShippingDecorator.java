package com.alotra.service.pricing.decorator;

import com.alotra.service.pricing.PriceComponent;
import com.alotra.service.pricing.PriceDecorator;
import java.math.BigDecimal;

public class ShippingDecorator extends PriceDecorator {
    private final BigDecimal shippingFee;

    public ShippingDecorator(PriceComponent wrapped, BigDecimal shippingFee) {
        super(wrapped);
        this.shippingFee = shippingFee != null ? shippingFee : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculate() {
        return wrapped.calculate().add(shippingFee);
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + Phí giao hàng (" + shippingFee + " ₫)";
    }
}
