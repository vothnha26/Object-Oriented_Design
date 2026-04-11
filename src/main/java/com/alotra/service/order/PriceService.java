package com.alotra.service.order;

import com.alotra.entity.Order;

public interface PriceService {
    void calculateTotal(Order order, String promotionCode);
}
