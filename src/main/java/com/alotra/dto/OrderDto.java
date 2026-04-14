package com.alotra.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDto {
    private Integer id;
    private LocalDateTime createdAt;
    private String status; // HTML Code (e.g. ChoXuLy)
    private String statusDisplay; // Tiếng Việt (e.g. Chờ xử lý)
    private BigDecimal total;
    private String customerName;
    private String customerPhone;
    private String paymentStatus; // HTML Code
    private String paymentStatusDisplay; // Tiếng Việt
    private String paymentMethod; // HTML Code
    private String paymentMethodDisplay; // Tiếng Việt
    private String shippingAddress;
    private String receivingMethod;
    private String receiverName;
    private com.alotra.entity.Employee employee;

    // Getters
    public Integer getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public String getStatusDisplay() { return statusDisplay; }
    public BigDecimal getTotal() { return total; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getPaymentStatusDisplay() { return paymentStatusDisplay; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentMethodDisplay() { return paymentMethodDisplay; }
    public String getShippingAddress() { return shippingAddress; }
    public String getReceivingMethod() { return receivingMethod; }
    public String getReceiverName() { return receiverName; }
    public com.alotra.entity.Employee getEmployee() { return employee; }

    // Setters
    public void setId(Integer id) { this.id = id; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setStatus(String status) { this.status = status; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setPaymentStatusDisplay(String paymentStatusDisplay) { this.paymentStatusDisplay = paymentStatusDisplay; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setPaymentMethodDisplay(String paymentMethodDisplay) { this.paymentMethodDisplay = paymentMethodDisplay; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setReceivingMethod(String receivingMethod) { this.receivingMethod = receivingMethod; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public void setEmployee(com.alotra.entity.Employee employee) { this.employee = employee; }
}
