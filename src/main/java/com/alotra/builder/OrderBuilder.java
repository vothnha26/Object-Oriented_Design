package com.alotra.builder;

import com.alotra.entity.Customer;
import com.alotra.entity.Order;
import com.alotra.entity.Payment;
import com.alotra.entity.ShippingInfo;
import com.alotra.entity.enums.PaymentMethod;
import com.alotra.entity.enums.ReceivingMethod;

import java.math.BigDecimal;

public class OrderBuilder {
    private Customer customer;
    private String paymentMethodStr;
    private String receivingMethodStr;
    private String note;
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal shippingFee = BigDecimal.ZERO;

    // Private constructor để chặn việc dùng từ khóa new
    private OrderBuilder() {
    }

    // Static method để khởi tạo Builder
    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    public OrderBuilder forCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public OrderBuilder payBy(String method) {
        this.paymentMethodStr = method;
        return this;
    }

    public OrderBuilder receivingMethod(String method) {
        this.receivingMethodStr = method;
        return this;
    }

    public OrderBuilder shipTo(String name, String phone, String address) {
        this.receiverName = name;
        this.receiverPhone = phone;
        this.shippingAddress = address;
        return this;
    }

    public OrderBuilder withNote(String note) {
        this.note = note;
        return this;
    }

    public OrderBuilder withSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        return this;
    }

    public OrderBuilder withDiscount(BigDecimal discount) {
        this.discount = discount != null ? discount : BigDecimal.ZERO;
        return this;
    }

    public OrderBuilder withShippingFee(BigDecimal fee) {
        this.shippingFee = fee != null ? fee : BigDecimal.ZERO;
        return this;
    }

    public Order build() {
        if (customer == null) {
            throw new IllegalStateException("Thiếu thông tin khách hàng");
        }

        Order order = new Order();
        order.setCustomer(customer);

        Payment payment = new Payment();
        if (paymentMethodStr != null) {
            try {
                payment.setMethod(PaymentMethod.valueOf(paymentMethodStr.toUpperCase()));
            } catch (Exception ignored) {
            }
        }
        order.setPayment(payment);

        ShippingInfo shipping = new ShippingInfo();
        StringBuilder orderNote = new StringBuilder();
        if (note != null && !note.isBlank()) {
            orderNote.append(note.trim());
        }

        boolean isDelivery = "Ship".equalsIgnoreCase(receivingMethodStr);
        if (isDelivery) {
            shipping.setMethod(ReceivingMethod.DELIVERY);
            String recvName = (receiverName != null && !receiverName.isBlank()) ? receiverName.trim() : (customer.getFullName() != null ? customer.getFullName().trim() : null);
            String recvPhone = (receiverPhone != null && !receiverPhone.isBlank()) ? receiverPhone.trim() : (customer.getPhone() != null ? customer.getPhone().trim() : null);
            String recvAddr = (shippingAddress != null && !shippingAddress.isBlank()) ? shippingAddress.trim() : null;

            if (recvPhone == null || recvPhone.isBlank() || recvAddr == null || recvAddr.isBlank()) {
                throw new IllegalArgumentException("Vui lòng nhập đầy đủ SĐT và Địa chỉ khi chọn Ship tận nơi");
            }

            shipping.setReceiverName(recvName);
            shipping.setReceiverPhone(recvPhone);
            shipping.setShippingAddress(recvAddr);

            if (orderNote.length() > 0) orderNote.append(" | ");
            orderNote.append("Ship to: ");
            if (recvName != null && !recvName.isBlank()) orderNote.append(recvName).append(", ");
            orderNote.append(recvPhone).append(", ").append(recvAddr);
        } else {
            shipping.setMethod(ReceivingMethod.PICKUP);
        }
        order.setShippingInfo(shipping);

        if (orderNote.length() > 0) {
            order.setNote(orderNote.toString());
        }

        // Default values for mandatory currency fields to avoid DB errors
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (discount == null) discount = BigDecimal.ZERO;
        if (shippingFee == null) shippingFee = BigDecimal.ZERO;

        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(subtotal.add(shippingFee).subtract(discount));

        return order;
    }
}
