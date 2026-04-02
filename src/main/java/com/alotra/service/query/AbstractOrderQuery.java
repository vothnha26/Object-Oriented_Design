package com.alotra.service.query;

import com.alotra.entity.Order;
import com.alotra.repository.OrderRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractOrderQuery {
    protected final OrderRepository repository;

    protected AbstractOrderQuery(OrderRepository repository) {
        this.repository = repository;
    }

    public abstract List<Order> execute(Object param1, Object param2);

    protected List<Order> sortOrders(List<Order> orders) {
        return orders.stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
}
