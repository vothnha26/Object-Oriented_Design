package com.alotra.service.query;

import com.alotra.dto.OrderDto;
import com.alotra.entity.Order;
import com.alotra.repository.OrderRepository;

import java.time.LocalDateTime;
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
        return execute(keyword, null, null, null, limit);
    }

    /**
     * Nâng cấp execute để hỗ trợ lọc đa năng.
     */
    public final List<OrderDto> execute(String keyword, String status, LocalDateTime from, LocalDateTime to, Integer limit) {
        List<Order> orders = fetchOrders();

        // 1. Lọc theo Base Filter (được định nghĩa ở lớp con)
        orders = orders.stream()
                .filter(o -> getFilter().matches(o))
                .collect(Collectors.toList());

        // 2. Lọc theo trạng thái
        if (status != null && !status.isBlank()) {
            OrderFilterStrategy statusFilter = new StatusOrderFilter(com.alotra.entity.enums.OrderStatus.valueOf(status));
            orders = orders.stream().filter(statusFilter::matches).collect(Collectors.toList());
        }

        // 3. Lọc theo khoảng thời gian
        if (from != null || to != null) {
            OrderFilterStrategy dateFilter = new DateRangeFilter(from, to);
            orders = orders.stream().filter(dateFilter::matches).collect(Collectors.toList());
        }

        // 4. Lọc theo từ khóa (ID hoặc tên khách)
        if (keyword != null && !keyword.isBlank()) {
            OrderFilterStrategy kwFilter = new KeywordFilter(keyword);
            orders = orders.stream().filter(kwFilter::matches).collect(Collectors.toList());
        }

        // 5. Sắp xếp
        orders.sort(getComparator());

        // 6. Giới hạn số lượng
        if (limit != null && limit > 0 && orders.size() > limit) {
            orders = orders.subList(0, limit);
        }

        // 7. Chuyển đổi sang DTO
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
