package com.alotra.service.query;

import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;

public class StatusOrderFilter implements OrderFilterStrategy {
    private final OrderStatus status;

    public StatusOrderFilter(OrderStatus status) {
        this.status = status;
    }

    @Override
    public boolean matches(Order order) {
        return status != null && status.equals(order.getStatus());
    }
}
