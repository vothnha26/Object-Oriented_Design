package com.alotra.service;

import com.alotra.dto.OrderDto;
import com.alotra.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import com.alotra.repository.OrderRepository;
import com.alotra.service.query.*;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderHistoryService {

    @PersistenceContext
    private EntityManager em;
    
    private final OrderRepository orderRepository;
 
    public OrderHistoryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderDto> listOrdersByCustomer(Integer customerId, String status) {
        return listOrdersByCustomer(customerId, status, null, null, null);
    }
 
    public List<OrderDto> listOrdersByCustomer(final Integer customerId, final String status, final Integer orderId,
                                                final LocalDateTime from, final LocalDateTime to) {
        AbstractOrderQuery query = new AbstractOrderQuery(orderRepository) {
            @Override
            protected OrderFilterStrategy getFilter() {
                List<OrderFilterStrategy> filters = new ArrayList<>();
                filters.add(new CustomerOrderFilter(customerId));
                
                if (status != null && !status.isBlank()) {
                    try {
                        filters.add(new StatusOrderFilter(com.alotra.entity.enums.OrderStatus.valueOf(status)));
                    } catch (Exception ignored) {}
                }
                
                if (orderId != null) {
                    filters.add(new OrderIdFilter(orderId));
                }
                
                if (from != null || to != null) {
                    filters.add(new DateRangeFilter(from, to));
                }
                
                return o -> filters.stream().allMatch(f -> f.matches(o));
            }
            
            @Override
            protected List<Order> fetchOrders() {
                // Optimization: fetch only customer orders
                return repository.findByCustomerId(customerId);
            }
        };
        
        return query.execute(null, null);
    }

    public OrderDto getOrderOfCustomer(Integer orderId, Integer customerId) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT dh FROM Order dh WHERE dh.id = :id AND dh.customer.id = :cid", Order.class);
        q.setParameter("id", orderId);
        q.setParameter("cid", customerId);
        List<Order> list = q.getResultList();
        if (list.isEmpty()) return null;
        
        // Use AbstractOrderQuery logic for mapping
        return new AbstractOrderQuery(orderRepository) {
            @Override protected OrderFilterStrategy getFilter() { return o -> true; }
        }.toDto(list.get(0));
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
            r.lineDiscount = ct.getLineDiscount();
            r.lineTotal = ct.getLineTotal();
            r.note = ct.getNote();
            out.add(r);
        }
        return out;
    }

    public List<ItemToppingRow> listOrderedToppings(Integer orderItemId) {
        TypedQuery<com.alotra.entity.OrderedTopping> q = em.createQuery(
                "SELECT t FROM OrderedTopping t JOIN FETCH t.topping tp WHERE t.orderLine.id = :lid ORDER BY tp.name",
                com.alotra.entity.OrderedTopping.class);
        q.setParameter("lid", orderItemId);
        List<com.alotra.entity.OrderedTopping> rows = q.getResultList();
        List<ItemToppingRow> out = new ArrayList<>();
        for (com.alotra.entity.OrderedTopping t : rows) {
            ItemToppingRow r = new ItemToppingRow();
            com.alotra.entity.Topping tp = t.getTopping();
            r.toppingName = tp != null ? tp.getName() : null;
            r.quantity = t.getQuantity();
            r.unitPrice = t.getUnitPrice();
            r.total = t.getLineTotal();
            out.add(r);
        }
        return out;
    }

    public OrderDto getOrder(Integer orderId) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT dh FROM Order dh LEFT JOIN FETCH dh.employee WHERE dh.id = :id", Order.class);
        q.setParameter("id", orderId);
        List<Order> list = q.getResultList();
        if (list.isEmpty()) return null;
        
        return new AbstractOrderQuery(orderRepository) {
            @Override protected OrderFilterStrategy getFilter() { return o -> true; }
        }.toDto(list.get(0));
    }

    public static class OrderItemRow {
        public Integer id;
        public String productName;
        public String sizeName;
        public Integer quantity;
        public java.math.BigDecimal unitPrice;
        public java.math.BigDecimal lineDiscount;
        public java.math.BigDecimal lineTotal;
        public String note;
    }

    public static class ItemToppingRow {
        public String toppingName;
        public Integer quantity;
        public java.math.BigDecimal unitPrice;
        public java.math.BigDecimal total;
    }
}