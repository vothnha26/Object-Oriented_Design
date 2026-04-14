package com.alotra.service.pricing.processor;

import com.alotra.entity.Order;
import com.alotra.service.pricing.PriceComponent;
import com.alotra.service.pricing.OrderPriceProcessor;
import com.alotra.service.pricing.decorator.RoundingDecorator;
import org.springframework.stereotype.Component;

/**
 * Trạm cuối (100): Luôn thực hiện làm tròn giá trị cuối cùng.
 */
@Component
public class RoundingProcessor implements OrderPriceProcessor {
    @Override
    public int getOrder() { return 100; }

    @Override
    public PriceComponent process(PriceComponent chain, Order order, String promoCode) {
        return new RoundingDecorator(chain);
    }
}
