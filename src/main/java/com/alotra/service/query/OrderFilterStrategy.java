package com.alotra.service.query;

import com.alotra.entity.Order;

@FunctionalInterface
public interface OrderFilterStrategy {
    boolean matches(Order order);
}
