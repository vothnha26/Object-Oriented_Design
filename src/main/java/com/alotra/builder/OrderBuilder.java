package com.alotra.builder;

import com.alotra.entity.Customer;
import com.alotra.entity.Employee;
import com.alotra.entity.Order;
import com.alotra.entity.OrderItem;
import com.alotra.entity.Payment;
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
    private OrderStatus status = OrderStatus.PENDING;
    private List<OrderItem> items = new ArrayList<>();
    private LocalDateTime createdAt = LocalDateTime.now();

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

    public OrderBuilder withItems(List<OrderItem> items) {
        this.items = items;
        return this;
    }

    public Order build() {
        Order order = new Order();
        order.setCustomer(customer);
        order.setApprovedBy(employee);
        order.setPromotion(promotion);
        order.setStatus(status);
        order.setCreatedAt(createdAt);
        
        if (items != null) {
            order.setItems(items);
            items.forEach(item -> item.setOrder(order));
        }

        return order;
    }
}
