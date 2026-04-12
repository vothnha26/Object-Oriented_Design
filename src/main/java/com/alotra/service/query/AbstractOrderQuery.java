package com.alotra.service.query;

import com.alotra.dto.OrderDto;
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

    /**
     * TEMPLATE METHOD: Pipeline truy vấn cố định.
     */
    public final List<OrderDto> execute(String keyword, Integer limit) {
        List<Order> orders = fetchOrders();

        orders = orders.stream()
                .filter(o -> getFilter().matches(o))
                .collect(Collectors.toList());

        if (keyword != null && !keyword.isBlank()) {
            OrderFilterStrategy kwFilter = new KeywordFilter(keyword);
            orders = orders.stream()
                    .filter(kwFilter::matches)
                    .collect(Collectors.toList());
        }

        orders.sort(getComparator());

        if (limit != null && limit > 0 && orders.size() > limit) {
            orders = orders.subList(0, limit);
        }

        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }

    protected abstract OrderFilterStrategy getFilter();
    protected abstract OrderDto toDto(Order order);

    protected List<Order> fetchOrders() {
        return repository.findAll();
    }

    protected Comparator<Order> getComparator() {
        return (a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt());
    }
}
