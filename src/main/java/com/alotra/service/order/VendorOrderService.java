package com.alotra.service.order;

import com.alotra.dto.OrderDto;
import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.repository.OrderRepository;
import org.springframework.stereotype.Service;
import com.alotra.service.query.AvailableOrderQuery;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VendorOrderService {
    private final OrderRepository orderRepository;
    private final AvailableOrderQuery availableOrderQuery;

    public VendorOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.availableOrderQuery = new AvailableOrderQuery(orderRepository);
    }

    public List<OrderDto> listAllOrders() {
        return orderRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<OrderDto> listTodayOrders() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return orderRepository.findByCreatedAtBetween(start, end).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<OrderDto> listOrders(String status, String kw, Integer limit) {
        // Sử dụng Template Method Pipeline nếu là trạng thái PENDING
        if (OrderStatus.PENDING.name().equalsIgnoreCase(status)) {
            return availableOrderQuery.execute(kw, limit);
        }

        // Fallback cho các status khác (hoặc tạo thêm Concrete Queries tương tự)
        List<Order> list = orderRepository.findAll();
        return list.stream()
                .filter(o -> status == null || status.isBlank() || o.getStatus().name().equalsIgnoreCase(status))
                .filter(o -> kw == null || kw.isBlank() || o.getId().toString().contains(kw) ||
                        (o.getCustomer() != null
                                && o.getCustomer().getFullName().toLowerCase().contains(kw.toLowerCase())))
                .limit(limit != null ? limit : Long.MAX_VALUE)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getDashboardCounts() {
        Map<String, Long> counts = new HashMap<>();
        List<Order> all = orderRepository.findAll();
        counts.put("TOTAL", (long) all.size());
        counts.put("PENDING", all.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count());
        counts.put("DELIVERED", all.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count());
        return counts;
    }

    @Transactional
    public void updateStatus(Integer orderId, OrderStatus status) {
        orderRepository.findById(orderId).ifPresent(o -> {
            // Sử dụng State Pattern để quản lý việc chuyển trạng thái
            com.alotra.entity.state.OrderContext ctx = new com.alotra.entity.state.OrderContext(o);

            // Nếu status truyền vào là CANCELLED, thực hiện cancel()
            if (status == OrderStatus.CANCELLED) {
                ctx.cancel();
            } else {
                // Giả định tiến tới trạng thái tiếp theo (advance)
                // hoặc bạn có thể logic set trực tiếp qua StateFactory nếu muốn nhảy cóc
                o.setStatus(status);
            }
            orderRepository.save(o);
        });
    }

    public OrderStatus nextStatus(OrderStatus current) {
        if (current == OrderStatus.PENDING)
            return OrderStatus.PREPARING;
        if (current == OrderStatus.PREPARING)
            return OrderStatus.DELIVERING;
        if (current == OrderStatus.DELIVERING)
            return OrderStatus.DELIVERED;
        return current;
    }

    public boolean canCancel(String status) {
        return OrderStatus.PENDING.name().equalsIgnoreCase(status);
    }

    private OrderDto toDto(Order o) {
        OrderDto dto = new OrderDto();
        dto.setId(o.getId());
        dto.setCreatedAt(o.getCreatedAt());
        dto.setStatus(o.getStatus().name());
        dto.setTotal(o.calculateTotal());
        if (o.getCustomer() != null) {
            dto.setCustomerName(o.getCustomer().getFullName());
            dto.setCustomerPhone(o.getCustomer().getPhone());
        }
        return dto;
    }

    public List<Order> findByVendor(Integer vendorId) {
        // Current domain doesn't link vendor to order directly yet,
        // returning all as placeholder for vendor-centric view
        return orderRepository.findAll();
    }
}
