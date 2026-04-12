package com.alotra.service.pricing;

import com.alotra.entity.Promotion;
import com.alotra.entity.enums.PromotionType;
import org.springframework.stereotype.Component;

@Component
public class ValuePromotionApplicator implements PromotionApplicator {
    @Override
    public boolean supports(PromotionType type) {
        return type == PromotionType.VALUE;
    }

    @Override
    public PriceComponent apply(PriceComponent base, Promotion promotion) {
        return new ValueDiscountDecorator(base, promotion.getDiscountValue());
    }
}
