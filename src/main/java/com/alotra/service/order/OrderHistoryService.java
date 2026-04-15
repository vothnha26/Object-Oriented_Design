package com.alotra.service.order;

import com.alotra.dto.OrderDto;
import com.alotra.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import com.alotra.repository.OrderRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderHistoryService {

    @PersistenceContext
    private EntityManager em;
    
    private final OrderRepository orderRepository;
 
    public OrderHistoryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderDto> listOrdersByCustomer(Integer customerId, String status) {
        List<Order> orders;
        if (status != null && !status.isBlank()) {
            orders = orderRepository.findByCustomerIdAndStatus(customerId, com.alotra.entity.enums.OrderStatus.valueOf(status));
        } else {
            orders = orderRepository.findByCustomerId(customerId);
        }
        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }

    public OrderDto getOrderOfCustomer(Integer orderId, Integer customerId) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT dh FROM Order dh WHERE dh.id = :id AND dh.customer.id = :cid", Order.class);
        q.setParameter("id", orderId);
        q.setParameter("cid", customerId);
        List<Order> list = q.getResultList();
        return list.isEmpty() ? null : toDto(list.get(0));
    }

    private OrderDto toDto(Order o) {
        OrderDto dto = new OrderDto();
        dto.setId(o.getId());
        dto.setCreatedAt(o.getCreatedAt());
        
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
        
        dto.setShippingAddress(""); // Field removed from Order entity

        return dto;
    }

    public List<OrderItemRow> listOrderItems(Integer orderId) {
        TypedQuery<OrderItem> q = em.createQuery(
                "SELECT ct FROM OrderItem ct " +
                "JOIN FETCH ct.variant v " +
                "JOIN FETCH v.product p " +
                "LEFT JOIN FETCH v.size s " +
                "WHERE ct.order.id = :oid ORDER BY ct.id", OrderItem.class);
        q.setParameter("oid", orderId);
        List<OrderItem> items = q.getResultList();
        List<OrderItemRow> out = new ArrayList<>();
        for (OrderItem ct : items) {
            OrderItemRow r = new OrderItemRow();
            r.id = ct.getId();
            ProductVariant v = ct.getVariant();
            Product p = v != null ? v.getProduct() : null;
            ProductSize sz = v != null ? v.getSize() : null;
            r.productName = p != null ? p.getName() : null;
            r.sizeName = sz != null ? sz.getName() : null;
            r.quantity = ct.getQuantity();
            r.unitPrice = ct.getUnitPrice();
            r.lineTotal = ct.getLineTotal();
            r.note = ct.getNote();
            out.add(r);
        }
        return out;
    }

    public List<ItemToppingRow> listOrderedToppings(Integer orderItemId) {
        TypedQuery<com.alotra.entity.OrderedTopping> q = em.createQuery(
                "SELECT t FROM OrderedTopping t JOIN FETCH t.topping tp WHERE t.orderItem.id = :lid ORDER BY tp.name",
                com.alotra.entity.OrderedTopping.class);
        q.setParameter("lid", orderItemId);
        List<com.alotra.entity.OrderedTopping> rows = q.getResultList();
        List<ItemToppingRow> out = new ArrayList<>();
        for (com.alotra.entity.OrderedTopping t : rows) {
            ItemToppingRow r = new ItemToppingRow();
            com.alotra.entity.Topping tp = t.getTopping();
            r.toppingName = tp != null ? tp.getName() : null;
            r.quantity = t.getQuantity();
            r.unitPrice = t.getPrice();
            r.total = t.getToppingTotal();
            out.add(r);
        }
        return out;
    }

    public OrderDto getOrder(Integer orderId) {
        return orderRepository.findById(orderId).map(this::toDto).orElse(null);
    }

    public static class OrderItemRow {
        public Integer id;
        public String productName;
        public String sizeName;
        public Integer quantity;
        public java.math.BigDecimal unitPrice;
        public java.math.BigDecimal lineTotal;
        public String note;
    }

    public static class ItemToppingRow {
        public String toppingName;
        public Integer quantity;
        public java.math.BigDecimal unitPrice;
        public java.math.BigDecimal total;
    }
    public List<Order> findByCustomer(Integer customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
}
