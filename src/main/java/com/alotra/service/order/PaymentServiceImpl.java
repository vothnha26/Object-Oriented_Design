package com.alotra.service.order;

import com.alotra.entity.Order;
import com.alotra.entity.enums.PaymentMethod;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Override
    public void processPayment(Order order, String method) {
        if (method != null) {
            try {
                order.getPayment().setMethod(PaymentMethod.valueOf(method.toUpperCase()));
            } catch (Exception e) {
                order.getPayment().setMethod(PaymentMethod.CASH);
            }
        }
        // Logic thực hiện thanh toán qua cổng thanh toán (mock)
        System.out.println("Processing payment for Order: " + order.getId() + " via " + method);
    }
}
