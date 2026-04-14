package com.alotra.service.order;

import com.alotra.dto.OrderDto;
import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.state.OrderContext;
import com.alotra.repository.OrderRepository;
import com.alotra.service.command.OrderCommandInvoker;
import com.alotra.service.command.UpdateOrderStatusCommand;
import org.springframework.stereotype.Service;
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
    private final OrderCommandInvoker orderCommandInvoker;

    public VendorOrderService(OrderRepository orderRepository, OrderCommandInvoker orderCommandInvoker) {
        this.orderRepository = orderRepository;
        this.orderCommandInvoker = orderCommandInvoker;
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
        List<Order> list = orderRepository.findAll();
        return list.stream()
                .filter(o -> status == null || status.isBlank() || o.getStatus().name().equalsIgnoreCase(status))
                .filter(o -> kw == null || kw.isBlank() || o.getId().toString().contains(kw) || 
                            (o.getCustomer() != null && o.getCustomer().getFullName().toLowerCase().contains(kw.toLowerCase())))
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
    public boolean advance(Integer orderId) {
        try {
            orderCommandInvoker.execute(UpdateOrderStatusCommand.advance(orderRepository, orderId));
            return true;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return false;
        }
    }

    @Transactional
    public boolean cancel(Integer orderId) {
        try {
            orderCommandInvoker.execute(UpdateOrderStatusCommand.cancel(orderRepository, orderId));
            return true;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return false;
        }
    }

    public boolean canCancel(Order order) {
        return order != null && new OrderContext(order).canCancel();
    }

    public boolean undoLastStatusChange() {
        return orderCommandInvoker.undo();
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
