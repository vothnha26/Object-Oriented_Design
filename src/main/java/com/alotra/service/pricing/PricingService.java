package com.alotra.service.pricing;

import com.alotra.entity.Order;
import com.alotra.entity.Promotion;
import com.alotra.entity.PromotionDetail;
import com.alotra.discount.DiscountStrategy;
import org.springframework.stereotype.Service;
import com.alotra.entity.OrderItem;

import java.math.BigDecimal;

import com.alotra.entity.PercentagePromotionDetail;
import com.alotra.entity.ValuePromotionDetail;
import com.alotra.discount.PercentDiscountStrategy;
import com.alotra.discount.FixedAmountDiscountStrategy;
import com.alotra.discount.NoDiscountStrategy;

/**
 * Service chuyên trách tính toán giá (Giao phó từ Order Entity).
 * Đã được refactor để tách biệt hoàn toàn logic khỏi Entity.
 */
@Service
public class PricingService {

    /**
     * Xây dựng pipeline tính giá cho đơn hàng.
     */
    public PriceComponent getPricePipeline(Order order) {
        // 1. Tính toán Subtotal (Dời logic từ OrderItem/OrderedTopping sang Service)
        BigDecimal subtotal = order.getItems().stream()
                .map(this::calculateItemSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Khởi tạo giá gốc (BasePrice)
        PriceComponent pipeline = new BasePrice(subtotal);

        // 3. Nếu có khuyến mãi, lựa chọn Strategy toán học phù hợp
        Promotion promotion = order.getPromotion();
        if (promotion != null && promotion.getDetail() != null) {
            pipeline = new PromotionDecorator(pipeline, createStrategy(promotion.getDetail()));
        }

        return pipeline;
    }

    /**
     * Tính thành tiền cho một OrderItem (bao gồm cả Toppings).
     */
    private BigDecimal calculateItemSubtotal(OrderItem item) {
        BigDecimal toppingsTotal = item.getToppings().stream()
                .map(ot -> ot.getPrice().multiply(BigDecimal.valueOf(ot.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return item.getUnitPrice().add(toppingsTotal).multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    /**
     * Tính toán tổng tiền cuối cùng cho đơn hàng.
     */
    public BigDecimal calculateFinalTotal(Order order) {
        BigDecimal total = getPricePipeline(order).calculate();
        return total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
    }

    /**
     * Tính toán tổng giá gốc (Subtotal) trước khi giảm giá.
     */
    public BigDecimal calculateSubtotal(Order order) {
        return order.getItems().stream()
                .map(this::calculateItemSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Lựa chọn Strategy toán học dựa trên loại thực thể PromotionDetail.
     * Không còn dùng Anonymous Class hay "người vận chuyển" trung gian.
     */
    private DiscountStrategy createStrategy(PromotionDetail detail) {
        if (detail instanceof PercentagePromotionDetail pct) {
            return new PercentDiscountStrategy(pct.getDiscountRate(), pct.getMaxDiscountAmount());
        } else if (detail instanceof ValuePromotionDetail val) {
            return new FixedAmountDiscountStrategy(val.getDiscountValue());
        }
        return new NoDiscountStrategy();
    }
}
