package com.alotra.service.order;

import com.alotra.entity.Order;
import com.alotra.entity.OrderItem;
import com.alotra.entity.Payment;
import com.alotra.service.pricing.PricingService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class PriceServiceImpl implements PriceService {

    private final PricingService pricingService;

    public PriceServiceImpl(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @Override
    public void calculateTotal(Order order, String promotionCode) {
        // Giao phó hoàn toàn việc tính toán cho PricingService (sử dụng Decorator Pattern)
        BigDecimal finalTotal = pricingService.calculateFinalTotal(order);

        // Lưu kết quả vào đối tượng Payment
        if (order.getPayment() == null) {
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(finalTotal);
            order.setPayment(payment);
        } else {
            order.getPayment().setAmount(finalTotal);
        }
    }

    @Override
    public void calculateItemTotal(OrderItem item) {
        // Line total logic remains in OrderItem.getLineTotal()
    }
}
