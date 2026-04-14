package com.alotra.service.pricing.strategy;

import com.alotra.entity.Promotion;
import com.alotra.entity.enums.PromotionType;
import com.alotra.service.pricing.PriceComponent;
import com.alotra.service.pricing.PromotionApplicator;
import com.alotra.service.pricing.decorator.PercentagePromotionDecorator;
import org.springframework.stereotype.Component;

@Component
public class PercentagePromotionApplicator implements PromotionApplicator {
    @Override
    public boolean supports(PromotionType type) {
        return type == PromotionType.PERCENTAGE;
    }

    @Override
    public PriceComponent apply(PriceComponent base, Promotion promotion) {
        return new PercentagePromotionDecorator(base, promotion.getDiscountRate());
    }
}
