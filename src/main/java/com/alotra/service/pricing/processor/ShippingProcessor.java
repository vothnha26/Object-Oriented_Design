package com.alotra.service.pricing.processor;

import com.alotra.entity.Order;
import com.alotra.service.pricing.PriceComponent;
import com.alotra.service.pricing.OrderPriceProcessor;
import com.alotra.service.pricing.decorator.ShippingDecorator;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Trạm 3: Cộng phí giao hàng vào chuỗi tính giá.
 */
@Component
public class ShippingProcessor implements OrderPriceProcessor {
    @Override
    public int getOrder() { return 30; }

    @Override
    public PriceComponent process(PriceComponent chain, Order order, String promoCode) {
        BigDecimal shippingFee = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
        return new ShippingDecorator(chain, shippingFee);
    }
}
