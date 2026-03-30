package com.alotra.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class OrderDto {
    private Integer id;
    private OffsetDateTime createdAt;
    private String status;
    private BigDecimal total;
    private String customerName;
    private String customerPhone;
    private String paymentStatus;
    private String paymentMethod;
    private String shippingAddress;
    private String receivingMethod;
    private String receiverName;
    private com.alotra.entity.Employee employee;

    // Getters
    public Integer getId() { return id; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getShippingAddress() { return shippingAddress; }
    public String getReceivingMethod() { return receivingMethod; }
    public String getReceiverName() { return receiverName; }
    public com.alotra.entity.Employee getEmployee() { return employee; }

    // Setters
    public void setId(Integer id) { this.id = id; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public void setStatus(String status) { this.status = status; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setReceivingMethod(String receivingMethod) { this.receivingMethod = receivingMethod; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public void setEmployee(com.alotra.entity.Employee employee) { this.employee = employee; }
}
