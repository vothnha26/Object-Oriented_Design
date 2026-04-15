package com.alotra.service.order;

import com.alotra.entity.Order;
import com.alotra.entity.enums.PaymentMethod;

public interface PaymentService {
    void processPayment(Order order, PaymentMethod method);
    void markAsPaid(Integer orderId, String transactionRef);
}
