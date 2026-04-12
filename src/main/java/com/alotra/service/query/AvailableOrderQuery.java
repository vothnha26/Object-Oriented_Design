package com.alotra.service.query;

import com.alotra.dto.OrderDto;
import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.repository.OrderRepository;

public class AvailableOrderQuery extends AbstractOrderQuery {

    public AvailableOrderQuery(OrderRepository repository) {
        super(repository);
    }

    @Override
    protected OrderFilterStrategy getFilter() {
        return new StatusOrderFilter(OrderStatus.PENDING);
    }

    @Override
    protected OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setStatus(order.getStatus().name());
        dto.setTotal(order.getTotalAmount());
        if (order.getCustomer() != null) {
            dto.setCustomerName(order.getCustomer().getFullName());
            dto.setCustomerPhone(order.getCustomer().getPhone());
        }
        return dto;
    }
}
