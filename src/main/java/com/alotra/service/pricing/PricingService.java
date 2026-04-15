package com.alotra.service.pricing;

import com.alotra.entity.Order;
import com.alotra.entity.Promotion;
import com.alotra.entity.PromotionDetail;
import com.alotra.discount.DiscountStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service chuyên trách tính toán giá (Giao phó từ Order Entity).
 * Sử dụng Decorator Pattern để xây dựng Pipeline và Strategy cho giảm giá.
 */
@Service
public class PricingService {

    /**
     * Xây dựng pipeline tính giá cho đơn hàng.
     */
    public PriceComponent getPricePipeline(Order order) {
        // 1. Tính toán Subtotal từ danh sách items (logic được dời từ Entity sang đây)
        BigDecimal subtotal = order.getItems().stream()
                .map(com.alotra.entity.OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Khởi tạo giá gốc (BasePrice)
        PriceComponent pipeline = new BasePrice(subtotal);

        // 3. Nếu có khuyến mãi, bọc bằng PromotionDecorator
        com.alotra.entity.Promotion promotion = order.getPromotion();
        if (promotion != null && promotion.getDetail() != null) {
            pipeline = new PromotionDecorator(pipeline, createStrategy(promotion));
        }

        // 3. Có thể thêm các Decorator khác ở đây (Thuế, phí ship...)
        // pipeline = new ShippingDecorator(pipeline, order.getShippingFee());

        return pipeline;
    }

    /**
     * Tính toán tổng tiền cuối cùng cho đơn hàng.
     */
    public BigDecimal calculateFinalTotal(Order order) {
        BigDecimal total = getPricePipeline(order).calculate();
        return total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
    }

    /**
     * Tạo Adapter từ PromotionDetail sang DiscountStrategy.
     */
    private DiscountStrategy createStrategy(final Promotion promotion) {
        final PromotionDetail detail = promotion.getDetail();
        return new DiscountStrategy() {
            @Override
            public BigDecimal apply(BigDecimal basePrice) {
                BigDecimal discount = detail.calculate(basePrice);
                return basePrice.subtract(discount);
            }

            @Override
            public String getName() {
                return promotion.getName();
            }
        };
    }
}
