package com.alotra.entity;

import com.alotra.entity.enums.ReceivingMethod;
import jakarta.persistence.*;

@Embeddable
public class ShippingInfo {

    @Column(name = "TenNguoiNhan")
    private String receiverName;

    @Column(name = "SDTNguoiNhan")
    private String receiverPhone;

    @Column(name = "DiaChiNhanHang")
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "PhuongThucNhanHang")
    private ReceivingMethod method;

    // === Business methods ===
    public boolean isValid() {
        if (method == ReceivingMethod.DELIVERY) {
            return receiverName != null && !receiverName.isBlank()
                    && receiverPhone != null && !receiverPhone.isBlank()
                    && shippingAddress != null && !shippingAddress.isBlank();
        }
        return true; // PICKUP doesn't require address
    }

    // Getters and setters
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public ReceivingMethod getMethod() { return method; }
    public void setMethod(ReceivingMethod method) { this.method = method; }
}
