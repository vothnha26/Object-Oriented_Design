package com.alotra.service.query;

import com.alotra.entity.Order;

public class OrderIdFilter implements OrderFilterStrategy {
    private final Integer orderId;

    public OrderIdFilter(Integer orderId) {
        this.orderId = orderId;
    }

    @Override
    public boolean matches(Order order) {
        return orderId.equals(order.getId());
    }
}
