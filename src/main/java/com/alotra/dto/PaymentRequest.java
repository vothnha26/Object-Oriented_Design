package com.alotra.dto;

import java.math.BigDecimal;

public class PaymentRequest {
    private Integer orderId;
    private BigDecimal amount;
    private String method; // e.g., CASH, BANK_TRANSFER, SEPAY
    private String returnUrl; // optional override

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
}
