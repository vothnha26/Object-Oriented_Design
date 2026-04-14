package com.alotra.service.pricing.strategy;

import com.alotra.entity.Promotion;
import com.alotra.entity.enums.PromotionType;
import com.alotra.service.pricing.PriceComponent;
import com.alotra.service.pricing.PromotionApplicator;
import com.alotra.service.pricing.decorator.ValuePromotionDecorator;
import org.springframework.stereotype.Component;

@Component
public class ValuePromotionApplicator implements PromotionApplicator {
    @Override
    public boolean supports(PromotionType type) {
        return type == PromotionType.VALUE;
    }

    @Override
    public PriceComponent apply(PriceComponent base, Promotion promotion) {
        return new ValuePromotionDecorator(base, promotion.getDiscountValue());
    }
}
