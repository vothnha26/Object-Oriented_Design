package com.alotra.service.order;

import com.alotra.entity.Order;

public interface PaymentService {
    void processPayment(Order order, String method);
}
