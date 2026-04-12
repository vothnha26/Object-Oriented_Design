package com.alotra.service.pricing;

import com.alotra.entity.Promotion;
import com.alotra.entity.enums.PromotionType;
import org.springframework.stereotype.Component;

@Component
public class PercentagePromotionApplicator implements PromotionApplicator {
    @Override
    public boolean supports(PromotionType type) {
        return type == PromotionType.PERCENTAGE;
    }

    @Override
    public PriceComponent apply(PriceComponent base, Promotion promotion) {
        return new PromotionDecorator(base, promotion.getDiscountRate());
    }
}
