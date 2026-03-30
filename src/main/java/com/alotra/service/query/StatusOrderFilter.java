package com.alotra.service.query;

import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;

public class StatusOrderFilter implements OrderFilterStrategy {
    private final OrderStatus targetStatus;

    public StatusOrderFilter(OrderStatus targetStatus) {
        this.targetStatus = targetStatus;
    }

    @Override
    public boolean matches(Order order) {
        return targetStatus.equals(order.getStatus());
    }
}
