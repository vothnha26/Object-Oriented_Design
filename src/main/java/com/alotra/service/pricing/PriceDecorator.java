package com.alotra.service.pricing;

public abstract class PriceDecorator implements PriceComponent {
    protected final PriceComponent wrapped;
    protected PriceDecorator(PriceComponent wrapped) {
        this.wrapped = wrapped;
    }
}
