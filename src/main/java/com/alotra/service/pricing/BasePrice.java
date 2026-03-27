package com.alotra.service.pricing;

import java.math.BigDecimal;

public class BasePrice implements PriceComponent {
    private final BigDecimal price;
    public BasePrice(BigDecimal price) {
        this.price = price;
    }
    @Override
    public BigDecimal calculate() { return price; }
    @Override
    public String getDescription() { return "Giá gốc"; }
}
