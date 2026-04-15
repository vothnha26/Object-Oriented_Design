package com.alotra.service.query;

import com.alotra.entity.Order;

public class AvailableOrderFilter implements OrderFilterStrategy {
    @Override
    public boolean matches(Order order) {
        return order.getApprovedBy() == null;
    }
}
