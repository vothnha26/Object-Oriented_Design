package com.alotra.dto;

import java.util.Map;

/**
 * Generic mapping for Sepay webhook/callback payload.
 * Adjust fields according to Sepay documentation.
 */
public class SepayCallbackDTO {
    private String merchantId;
    private String transactionId;
    private String orderId;
    private String status;
    private String signature;
    private Long amount;
    private Map<String, Object> raw;

    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public Map<String, Object> getRaw() { return raw; }
    public void setRaw(Map<String, Object> raw) { this.raw = raw; }
}
