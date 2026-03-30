package com.alotra.service.query;

import com.alotra.entity.Order;

public class CustomerOrderFilter implements OrderFilterStrategy {
    private final Integer customerId;

    public CustomerOrderFilter(Integer customerId) {
        this.customerId = customerId;
    }

    @Override
    public boolean matches(Order order) {
        return order.getCustomer() != null && customerId.equals(order.getCustomer().getId());
    }
}
