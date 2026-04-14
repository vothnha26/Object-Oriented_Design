package com.alotra.service.order;

import com.alotra.entity.Customer;
import com.alotra.entity.Employee;
import com.alotra.entity.Order;
import com.alotra.entity.OrderItem;
import com.alotra.entity.Promotion;
import com.alotra.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderBuilder {
    private Customer customer;
    private Employee employee;
    private Promotion promotion;
    private String shippingAddressLine;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private LocalDateTime createdAt = LocalDateTime.now();
    private OrderStatus status = OrderStatus.PENDING;
    private List<OrderItem> items = new ArrayList<>();

    private OrderBuilder() {
    }

    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    public OrderBuilder forCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public OrderBuilder handledBy(Employee employee) {
        this.employee = employee;
        return this;
    }

    public OrderBuilder withPromotion(Promotion promotion) {
        this.promotion = promotion;
        return this;
    }

    public OrderBuilder shipTo(String addressLine) {
        this.shippingAddressLine = addressLine;
        return this;
    }

    public OrderBuilder withDiscount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        return this;
    }

    public OrderBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        return this;
    }

    public OrderBuilder withStatus(OrderStatus status) {
        this.status = status != null ? status : OrderStatus.PENDING;
        return this;
    }

    public OrderBuilder withItems(List<OrderItem> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        return this;
    }

    public Order build() {
        if (customer == null) {
            throw new IllegalStateException("Customer is required to build an order");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setEmployee(employee);
        order.setPromotion(promotion);
        order.setShippingAddressLine(shippingAddressLine);
        order.setDiscountAmount(discountAmount);
        order.setCreatedAt(createdAt);
        order.setStatus(status);

        for (OrderItem item : items) {
            item.setOrder(order);
        }
        order.setItems(items);
        return order;
    }
}
