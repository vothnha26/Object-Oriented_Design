package com.alotra.service.query;

import com.alotra.dto.OrderDto;
import com.alotra.entity.Order;
import com.alotra.repository.OrderRepository;

public class CustomerOrderHistoryQuery extends AbstractOrderQuery {
    private final Integer customerId;

    public CustomerOrderHistoryQuery(OrderRepository repository, Integer customerId) {
        super(repository);
        this.customerId = customerId;
    }

    @Override
    protected java.util.List<Order> fetchOrders() {
        return repository.findByCustomerId(customerId);
    }

    @Override
    protected OrderFilterStrategy getFilter() {
        return new CustomerOrderFilter(customerId);
    }

    @Override
    protected OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setStatus(order.getStatus().name());
        dto.setTotal(order.getTotalAmount());
        return dto;
    }
}
