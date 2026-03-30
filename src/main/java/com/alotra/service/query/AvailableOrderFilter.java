package com.alotra.service.query;

import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;

public class AvailableOrderFilter implements OrderFilterStrategy {
    @Override
    public boolean matches(Order order) {
        return OrderStatus.PENDING.equals(order.getStatus()) && order.getEmployee() == null;
    }
}
