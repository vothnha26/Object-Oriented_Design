package com.alotra.service.order;

import com.alotra.dto.OrderDto;
import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.state.OrderContext;
import com.alotra.repository.OrderRepository;
import com.alotra.service.command.OrderCommandInvoker;
import com.alotra.service.command.UpdateOrderStatusCommand;
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
    private final OrderCommandInvoker orderCommandInvoker;

    public VendorOrderService(OrderRepository orderRepository, OrderCommandInvoker orderCommandInvoker) {
        this.orderRepository = orderRepository;
        this.availableOrderQuery = new AvailableOrderQuery(orderRepository);
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
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        
        counts.put("total", (long) all.size());
        counts.put("pending", all.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count());
        counts.put("preparing", all.stream().filter(o -> o.getStatus() == OrderStatus.PREPARING).count());
        counts.put("shipping", all.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERING).count());
        counts.put("today", all.stream().filter(o -> o.getCreatedAt().isAfter(startOfDay)).count());
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
        
        // Use Enum metadata instead of switch-case
        dto.setStatus(o.getStatus().getCode());
        dto.setStatusDisplay(o.getStatus().getDisplayName());
        
        dto.setTotal(o.getFinalTotal());
        if (o.getCustomer() != null) {
            dto.setCustomerName(o.getCustomer().getFullName());
            dto.setCustomerPhone(o.getCustomer().getPhone());
        }

        if (o.getPayment() != null) {
            dto.setPaymentStatus(o.getPayment().getStatus().getCode());
            dto.setPaymentStatusDisplay(o.getPayment().getStatus().getDisplayName());
            dto.setPaymentMethod(o.getPayment().getMethod().getCode());
            dto.setPaymentMethodDisplay(o.getPayment().getMethod().getDisplayName());
        } else {
            dto.setPaymentStatus(com.alotra.entity.enums.PaymentStatus.UNPAID.getCode());
            dto.setPaymentStatusDisplay(com.alotra.entity.enums.PaymentStatus.UNPAID.getDisplayName());
            dto.setPaymentMethod(com.alotra.entity.enums.PaymentMethod.CASH.getCode());
            dto.setPaymentMethodDisplay(com.alotra.entity.enums.PaymentMethod.CASH.getDisplayName());
        }

        return dto;
    }

    public List<Order> findByVendor(Integer vendorId) {
        // Current domain doesn't link vendor to order directly yet,
        // returning all as placeholder for vendor-centric view
        return orderRepository.findAll();
    }
}
