package com.alotra.service;

import com.alotra.entity.Order;
import com.alotra.entity.Employee;
import com.alotra.entity.ShippingInfo;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.enums.PaymentMethod;
import com.alotra.entity.enums.PaymentStatus;
<<<<<<< HEAD
import com.alotra.repository.OrderRepository;
import com.alotra.repository.EmployeeRepository;
import com.alotra.service.proxy.ShipperOrderOperations;
=======
import com.alotra.entity.state.OrderContext;
import com.alotra.repository.OrderRepository;
import com.alotra.repository.EmployeeRepository;
>>>>>>> feature/builder-pattern
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

<<<<<<< HEAD
@Service("shipperOrderOperationsReal")
public class ShipperOrderService implements ShipperOrderOperations {

=======
@Service
public class ShipperOrderService {
>>>>>>> feature/builder-pattern
    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;

    public ShipperOrderService(OrderRepository orderRepository, EmployeeRepository employeeRepository) {
        this.orderRepository = orderRepository;
        this.employeeRepository = employeeRepository;
    }

<<<<<<< HEAD
    @Override
=======
>>>>>>> feature/builder-pattern
    public Map<String, Object> getDashboardStats(Integer shipperId) {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime startOfWeek = LocalDateTime.of(LocalDate.now().minusDays(6), LocalTime.MIN);

        // 1. Delivering (Specific to this shipper)
        long shipping = orderRepository.countByEmployeeIdAndStatus(shipperId, OrderStatus.DELIVERING);

        // 2. Delivered today (Specific to this shipper)
        long deliveredToday = orderRepository.countByEmployeeIdAndStatusAndCreatedAtBetween(
                shipperId, OrderStatus.DELIVERED, startOfDay, endOfDay);

        // 3. Total assigned (Specific to this shipper)
        List<OrderStatus> assignedStatuses = List.of(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.DELIVERING);
        long totalAssigned = orderRepository.countByEmployeeIdAndStatusIn(shipperId, assignedStatuses);

        // 4. Delivered this week (7 days, specific to this shipper)
        long deliveredThisWeek = orderRepository.countByEmployeeIdAndStatusAndCreatedAtAfter(
                shipperId, OrderStatus.DELIVERED, startOfWeek);

        stats.put("shipping", shipping);
        stats.put("deliveredToday", deliveredToday);
        stats.put("totalAssigned", totalAssigned);
        stats.put("deliveredThisWeek", deliveredThisWeek);
        
        return stats;
    }

<<<<<<< HEAD
    @Override
=======
>>>>>>> feature/builder-pattern
    public List<OrderDto> getAssignedOrders(Integer shipperId, String status, String keyword, Integer limit) {
        final List<OrderStatus> targetStatuses;
        if (status != null && !status.isBlank()) {
            OrderStatus filteredStatus = null;
            try {
                filteredStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (Exception e) {
                // Ignore invalid status
            }
            if (filteredStatus != null && filteredStatus != OrderStatus.CANCELLED) {
                targetStatuses = List.of(filteredStatus);
            } else {
                targetStatuses = List.of(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.DELIVERING, OrderStatus.DELIVERED);
            }
        } else {
            targetStatuses = List.of(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.DELIVERING, OrderStatus.DELIVERED);
        }
        
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getEmployee() != null && java.util.Objects.equals(shipperId, o.getEmployee().getId()))
                .filter(o -> targetStatuses.contains(o.getStatus()))
                .filter(o -> !(o.getStatus() == OrderStatus.DELIVERED && o.getPayment().getStatus() == PaymentStatus.PAID))
                .collect(Collectors.toList());
        
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            orders = orders.stream()
                .filter(o -> {
                    String custName = o.getCustomer() != null && o.getCustomer().getFullName() != null 
                        ? o.getCustomer().getFullName().toLowerCase() : "";
                    ShippingInfo si = o.getShippingInfo();
                    String phone = si != null && si.getReceiverPhone() != null ? si.getReceiverPhone() : "";
                    String address = si != null && si.getShippingAddress() != null ? si.getShippingAddress().toLowerCase() : "";
                    return custName.contains(kw) || phone.contains(kw) || address.contains(kw);
                })
                .collect(Collectors.toList());
        }
        
        orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        if (limit != null && limit > 0 && orders.size() > limit) {
            orders = orders.subList(0, limit);
        }
        
        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }

<<<<<<< HEAD
    @Override
=======
>>>>>>> feature/builder-pattern
    public List<OrderDto> getAvailableOrders(String keyword, Integer limit) {
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING && o.getEmployee() == null)
                .collect(Collectors.toList());
        
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            orders = orders.stream()
                .filter(o -> {
                    String custName = o.getCustomer() != null && o.getCustomer().getFullName() != null 
                        ? o.getCustomer().getFullName().toLowerCase() : "";
                    ShippingInfo si = o.getShippingInfo();
                    String phone = si != null && si.getReceiverPhone() != null ? si.getReceiverPhone() : "";
                    String address = si != null && si.getShippingAddress() != null ? si.getShippingAddress().toLowerCase() : "";
                    return custName.contains(kw) || phone.contains(kw) || address.contains(kw);
                })
                .collect(Collectors.toList());
        }
        
        orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        if (limit != null && limit > 0 && orders.size() > limit) {
            orders = orders.subList(0, limit);
        }
        
        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }

<<<<<<< HEAD
    @Override
=======
>>>>>>> feature/builder-pattern
    public List<OrderDto> getTodayShippingOrders(Integer shipperId) {
        List<Order> orders = orderRepository.findAll().stream()
            .filter(o -> o.getStatus() == OrderStatus.DELIVERING)
            .collect(Collectors.toList());
        
        orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
<<<<<<< HEAD
    @Override
=======
>>>>>>> feature/builder-pattern
    public boolean markAsDelivered(Integer orderId, Integer shipperId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return false;
        
<<<<<<< HEAD
=======
        if (order.getEmployee() == null || !java.util.Objects.equals(order.getEmployee().getId(), shipperId)) {
            return false;
        }
        
>>>>>>> feature/builder-pattern
        if (order.getStatus() != OrderStatus.DELIVERING) {
            return false;
        }
        
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
        return true;
    }

    @Transactional
<<<<<<< HEAD
    @Override
=======
>>>>>>> feature/builder-pattern
    public boolean acceptOrder(Integer orderId, Integer shipperId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return false;
        if (order.getEmployee() != null) return false;
        
        Employee shipper = employeeRepository.findById(shipperId).orElse(null);
        if (shipper == null) return false;
        
        order.setEmployee(shipper);
        orderRepository.save(order);
        return true;
    }

    @Transactional
<<<<<<< HEAD
    @Override
=======
>>>>>>> feature/builder-pattern
    public boolean advanceOrder(Integer orderId, Integer shipperId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return false;
        
<<<<<<< HEAD
=======
        if (order.getEmployee() == null || !java.util.Objects.equals(shipperId, order.getEmployee().getId())) {
            return false;
        }
        
>>>>>>> feature/builder-pattern
        if (order.getPayment().getMethod() == PaymentMethod.BANK_TRANSFER
                && order.getPayment().getStatus() != PaymentStatus.PAID) {
            return false;
        }
        
<<<<<<< HEAD
        OrderStatus currentStatus = order.getStatus();
        OrderStatus nextStatus = getNextStatus(currentStatus);

        if (nextStatus != null && nextStatus != currentStatus) {
            order.setStatus(nextStatus);
            orderRepository.save(order);
            return true;
        }
        return false;
    }

    @Transactional
    @Override
=======
        try {
            OrderContext ctx = new OrderContext(order);
            ctx.advance();
            orderRepository.save(order);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @Transactional
>>>>>>> feature/builder-pattern
    public boolean advanceOrderSimple(Integer orderId, Integer shipperId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return false;
        
        if (order.getEmployee() == null) {
            Employee shipper = employeeRepository.findById(shipperId).orElse(null);
            if (shipper != null) {
                order.setEmployee(shipper);
            }
        }
        
        if (order.getPayment().getMethod() == PaymentMethod.BANK_TRANSFER
                && order.getPayment().getStatus() != PaymentStatus.PAID) {
            return false;
        }
        
<<<<<<< HEAD
        OrderStatus currentStatus = order.getStatus();
        OrderStatus nextStatus = getNextStatus(currentStatus);
        
        if (nextStatus != null && nextStatus != currentStatus) {
            order.setStatus(nextStatus);
            orderRepository.save(order);
            return true;
        }
        return false;
    }

    @Transactional
    @Override
=======
        try {
            OrderContext ctx = new OrderContext(order);
            ctx.advance();
            orderRepository.save(order);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @Transactional
>>>>>>> feature/builder-pattern
    public boolean cancelOrder(Integer orderId, Integer shipperId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return false;
        
<<<<<<< HEAD
        OrderStatus currentStatus = order.getStatus();
        if (canCancel(currentStatus)) {
            order.setStatus(OrderStatus.CANCELLED);
=======
        if (order.getEmployee() == null || !order.getEmployee().getId().equals(shipperId)) {
            return false;
        }
        
        OrderContext ctx = new OrderContext(order);
        if (ctx.canCancel()) {
            ctx.cancel();
>>>>>>> feature/builder-pattern
            orderRepository.save(order);
            return true;
        }
        return false;
    }

<<<<<<< HEAD
    private OrderStatus getNextStatus(OrderStatus currentStatus) {
        if (currentStatus == null) return null;
        return switch (currentStatus) {
            case PENDING -> OrderStatus.PREPARING;
            case PREPARING -> OrderStatus.DELIVERING;
            case DELIVERING -> OrderStatus.DELIVERED;
            default -> null;
        };
    }

    private boolean canCancel(OrderStatus status) {
        return status == OrderStatus.PENDING || status == OrderStatus.PREPARING;
    }

    @Override
    public boolean isOrderAssignedToShipper(Integer orderId, Integer shipperId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return false;
        return order.getEmployee() != null && java.util.Objects.equals(order.getEmployee().getId(), shipperId);
    }

    @Transactional
    @Override
    public boolean confirmPayment(Integer orderId, Integer shipperId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || order.getPayment() == null) {
            return false;
        }
        if (order.getPayment().getStatus() == PaymentStatus.PAID) {
            return false;
        }
        order.getPayment().setStatus(PaymentStatus.PAID);
        order.getPayment().setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
        return true;
=======
    public boolean isOrderAssignedToShipper(Integer orderId, Integer shipperId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return false;
        return order.getEmployee() != null && order.getEmployee().getId().equals(shipperId);
>>>>>>> feature/builder-pattern
    }

    private OrderDto toDto(Order o) {
        OrderDto dto = new OrderDto();
        dto.id = o.getId();
        dto.customerName = o.getCustomer() != null ? o.getCustomer().getFullName() : "N/A";
        dto.customerPhone = o.getCustomer() != null ? o.getCustomer().getPhone() : "N/A";
        ShippingInfo si = o.getShippingInfo();
        dto.receiverName = si != null ? si.getReceiverName() : null;
        dto.receiverPhone = si != null ? si.getReceiverPhone() : null;
        dto.shippingAddress = si != null ? si.getShippingAddress() : null;
        dto.createdAt = o.getCreatedAt();
        dto.status = o.getStatus() != null ? o.getStatus().name() : null;
        if (o.getPayment() != null) {
            dto.paymentStatus = o.getPayment().getStatus() != null ? o.getPayment().getStatus().name() : null;
            dto.paymentMethod = o.getPayment().getMethod() != null ? o.getPayment().getMethod().name() : null;
        } else {
            dto.paymentStatus = null;
            dto.paymentMethod = null;
        }
        dto.total = o.getTotalAmount();
        dto.note = o.getNote();
        dto.receivingMethod = si != null && si.getMethod() != null ? si.getMethod().name() : null;
        return dto;
    }

    public static class OrderDto {
        public Integer id;
        public String customerName;
        public String customerPhone;
        public String receiverName;
        public String receiverPhone;
        public String shippingAddress;
        public LocalDateTime createdAt;
        public String status;
        public String paymentStatus;
        public String paymentMethod;
        public java.math.BigDecimal total;
        public String note;
        public String receivingMethod;
    }
}
