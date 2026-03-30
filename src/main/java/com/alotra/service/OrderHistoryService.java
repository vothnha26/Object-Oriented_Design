package com.alotra.service;

import com.alotra.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class OrderHistoryService {
    private static final ZoneId HCM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @PersistenceContext
    private EntityManager em;

    public OrderHistoryService() {}

    public List<OrderRow> listOrdersByCustomer(Integer customerId, String status) {
        return listOrdersByCustomer(customerId, status, null, null, null);
    }

    public List<OrderRow> listOrdersByCustomer(Integer customerId, String status, Integer orderId,
                                               LocalDateTime from, LocalDateTime to) {
        StringBuilder jpql = new StringBuilder("SELECT dh FROM Order dh WHERE dh.customer.id = :cid");
        Map<String, Object> params = new HashMap<>();
        params.put("cid", customerId);
        if (status != null && !status.isBlank()) {
            jpql.append(" AND CAST(dh.status AS string) = :st");
            params.put("st", status);
        }
        if (orderId != null) {
            jpql.append(" AND dh.id = :oid");
            params.put("oid", orderId);
        }
        if (from != null) {
            jpql.append(" AND dh.createdAt >= :from");
            params.put("from", from);
        }
        if (to != null) {
            jpql.append(" AND dh.createdAt <= :to");
            params.put("to", to);
        }
        jpql.append(" ORDER BY dh.id DESC");
        TypedQuery<Order> q = em.createQuery(jpql.toString(), Order.class);
        params.forEach(q::setParameter);
        List<Order> list = q.getResultList();
        List<OrderRow> out = new ArrayList<>();
        for (Order dh : list) out.add(mapOrderRow(dh));
        return out;
    }

    public OrderRow getOrderOfCustomer(Integer orderId, Integer customerId) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT dh FROM Order dh WHERE dh.id = :id AND dh.customer.id = :cid", Order.class);
        q.setParameter("id", orderId);
        q.setParameter("cid", customerId);
        List<Order> list = q.getResultList();
        return list.isEmpty() ? null : mapOrderRow(list.get(0));
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
        TypedQuery<OrderedTopping> q = em.createQuery(
                "SELECT t FROM OrderedTopping t JOIN FETCH t.topping tp WHERE t.orderLine.id = :lid ORDER BY tp.name",
                OrderedTopping.class);
        q.setParameter("lid", orderItemId);
        List<OrderedTopping> rows = q.getResultList();
        List<ItemToppingRow> out = new ArrayList<>();
        for (OrderedTopping t : rows) {
            ItemToppingRow r = new ItemToppingRow();
            Topping tp = t.getTopping();
            r.toppingName = tp != null ? tp.getName() : null;
            r.quantity = t.getQuantity();
            r.unitPrice = t.getUnitPrice();
            r.total = t.getLineTotal();
            out.add(r);
        }
        return out;
    }

    public OrderRow getOrder(Integer orderId) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT dh FROM Order dh LEFT JOIN FETCH dh.employee WHERE dh.id = :id", Order.class);
        q.setParameter("id", orderId);
        List<Order> list = q.getResultList();
        Order dh = list.isEmpty() ? null : list.get(0);
        return dh == null ? null : mapOrderRow(dh);
    }

    private OrderRow mapOrderRow(Order dh) {
        OrderRow r = new OrderRow();
        r.id = dh.getId();
        LocalDateTime ts = dh.getCreatedAt();
        r.createdAt = ts != null ? OffsetDateTime.of(ts, HCM_ZONE.getRules().getOffset(ts)) : null;
        r.status = dh.getStatus() != null ? dh.getStatus().name() : null;
        r.paymentStatus = dh.getPayment().getStatus() != null ? dh.getPayment().getStatus().name() : null;
        r.paymentMethod = dh.getPayment().getMethod() != null ? dh.getPayment().getMethod().name() : null;
        r.total = dh.getTotalAmount();
        ShippingInfo si = dh.getShippingInfo();
        if (si != null) {
            r.receivingMethod = si.getMethod() != null ? si.getMethod().name() : null;
            r.receiverName = si.getReceiverName();
            r.receiverPhone = si.getReceiverPhone();
            r.shippingAddress = si.getShippingAddress();
        }
        r.employee = dh.getEmployee();
        return r;
    }

    public static class OrderRow {
        public Integer id;
        public java.time.OffsetDateTime createdAt;
        public String status;
        public String paymentStatus;
        public String paymentMethod;
        public java.math.BigDecimal total;
        public String receivingMethod;
        public String receiverName;
        public String receiverPhone;
        public String shippingAddress;
        public Employee employee;
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