package com.alotra.builder;

import com.alotra.entity.*;
import com.alotra.entity.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderBuilder {
    private Customer customer;
    private Promotion promotion;
    private String shippingAddressLine;
    private BigDecimal subTotal = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal shippingFee = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private OrderStatus status = OrderStatus.PENDING;
    private List<OrderItem> items = new ArrayList<>();

    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    public OrderBuilder forCustomer(Customer customer) {
        this.customer = customer;
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

    public OrderBuilder amounts(BigDecimal subTotal, BigDecimal discount, BigDecimal shipping, BigDecimal total) {
        this.subTotal = subTotal;
        this.discountAmount = discount;
        this.shippingFee = shipping;
        this.totalAmount = total;
        return this;
    }

    public OrderBuilder withItems(List<OrderItem> items) {
        this.items = items;
        return this;
    }

    public Order build() {
        Order order = new Order();
        order.setCustomer(customer);
        order.setPromotion(promotion);
        order.setShippingAddressLine(shippingAddressLine);
        order.setSubTotal(subTotal);
        order.setDiscountAmount(discountAmount);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(totalAmount);
        order.setStatus(status);
        order.setCreatedAt(LocalDateTime.now());
        
        if (items != null) {
            for (OrderItem item : items) {
                item.setOrder(order);
            }
            order.setItems(items);
        }
        return order;
    }
}
