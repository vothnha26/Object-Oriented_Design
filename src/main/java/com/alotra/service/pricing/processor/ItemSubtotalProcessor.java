package com.alotra.service.pricing.processor;

import com.alotra.entity.Order;
import com.alotra.entity.OrderItem;
import com.alotra.service.pricing.PriceComponent;
import com.alotra.service.pricing.OrderPriceProcessor;
import com.alotra.service.pricing.component.BasePrice;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Trạm 1: Tính tổng tiền các sản phẩm (Subtotal) làm gốc cho chuỗi tính giá.
 */
@Component
public class ItemSubtotalProcessor implements OrderPriceProcessor {
    @Override
    public int getOrder() { return 10; }

    @Override
    public PriceComponent process(PriceComponent chain, Order order, String promoCode) {
        BigDecimal subTotal = order.getItems().stream()
                .map(OrderItem::getLineTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        order.setSubTotal(subTotal);
        // Trạm đầu tiên thường khởi tạo BasePrice thay vì bọc tiếp
        return new BasePrice(subTotal);
    }
}
