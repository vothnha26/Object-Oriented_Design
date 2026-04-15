package com.alotra.service.pricing;

import java.math.BigDecimal;

/**
 * Lớp trừu tượng cho các thành phần giá bổ sung (Khuyến mãi, Phí ship, Thuế...).
 */
public abstract class PriceDecorator implements PriceComponent {
    protected final PriceComponent wrapped;

    protected PriceDecorator(PriceComponent wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public abstract BigDecimal calculate();

    @Override
    public String getDescription() {
        return wrapped.getDescription();
    }
}
