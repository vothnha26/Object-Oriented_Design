package com.alotra.service.query;

import com.alotra.dto.OrderDto;
import com.alotra.entity.Order;
import com.alotra.repository.OrderRepository;

public class CustomerOrderHistoryQuery extends AbstractOrderQuery {

    public CustomerOrderHistoryQuery(OrderRepository repository) {
        super(repository);
    }

    @Override
    protected OrderFilterStrategy getFilter() {
        return order -> true; // All for now
    }

    @Override
    protected OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setStatus(order.getStatus().name());
        dto.setTotal(order.getFinalTotal());
        return dto;
    }
}
