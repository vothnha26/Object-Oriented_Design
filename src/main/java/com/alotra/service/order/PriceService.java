package com.alotra.service.order;

import com.alotra.entity.Order;
import com.alotra.entity.OrderItem;

public interface PriceService {
    void calculateTotal(Order order, String promotionCode);
    void calculateItemTotal(OrderItem item);
}
