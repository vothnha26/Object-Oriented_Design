package com.alotra.service.query;

import com.alotra.dto.OrderDto;
import com.alotra.entity.Order;
import com.alotra.repository.OrderRepository;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractOrderQuery {

    protected final OrderRepository repository;

    protected AbstractOrderQuery(OrderRepository repository) {
        this.repository = repository;
    }

    // === TEMPLATE METHOD ===
    public final List<OrderDto> execute(String keyword, Integer limit) {
        List<Order> orders = fetchOrders();

        // 1. Core State Filter
        orders = orders.stream()
                .filter(o -> getFilter().matches(o))
                .collect(Collectors.toList());

        // 2. Keyword Search
        if (keyword != null && !keyword.isBlank()) {
            OrderFilterStrategy kwFilter = new KeywordFilter(keyword);
            orders = orders.stream()
                    .filter(kwFilter::matches)
                    .collect(Collectors.toList());
        }

        // 3. Sort
        orders.sort(getComparator());

        // 4. Limit
        if (limit != null && limit > 0 && orders.size() > limit) {
            orders = orders.subList(0, limit);
        }

        // 5. Map to DTO
        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }

    protected abstract OrderFilterStrategy getFilter();

    protected List<Order> fetchOrders() {
        return repository.findAll();
    }

    protected Comparator<Order> getComparator() {
        return (a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt());
    }

    public OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());

        if (order.getCreatedAt() != null) {
            java.time.ZoneOffset offset = ZoneId.of("Asia/Ho_Chi_Minh").getRules().getOffset(order.getCreatedAt());
            dto.setCreatedAt(
                    order.getCreatedAt().toInstant(offset).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toOffsetDateTime());
        }

        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setTotal(order.getTotalAmount());

        if (order.getCustomer() != null) {
            dto.setCustomerName(order.getCustomer().getFullName());
        }
        if (order.getShippingInfo() != null) {
            dto.setCustomerPhone(order.getShippingInfo().getReceiverPhone());
        }

        if (order.getPayment() != null) {
            dto.setPaymentStatus(order.getPayment().getStatus() != null ? order.getPayment().getStatus().name() : null);
            dto.setPaymentMethod(order.getPayment().getMethod() != null ? order.getPayment().getMethod().name() : null);
        }

        if (order.getShippingInfo() != null) {
            dto.setReceivingMethod(
                    order.getShippingInfo().getMethod() != null ? order.getShippingInfo().getMethod().name() : null);
            dto.setReceiverName(order.getShippingInfo().getReceiverName());
            dto.setShippingAddress(order.getShippingInfo().getShippingAddress());
        }

        dto.setEmployee(order.getEmployee());

        return dto;
    }
}
