package com.alotra.dto;

import java.util.List;

public class CheckoutRequest {
    private List<CartItemDTO> cartItems;
    private Integer addressId;
    private String shippingAddress; // Thêm trường để nhận địa chỉ text từ form
    private String paymentMethod;
    private String promotionCode;
    private String note;

    // Getters and Setters
    public List<CartItemDTO> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItemDTO> cartItems) { this.cartItems = cartItems; }
    public Integer getAddressId() { return addressId; }
    public void setAddressId(Integer addressId) { this.addressId = addressId; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPromotionCode() { return promotionCode; }
    public void setPromotionCode(String promotionCode) { this.promotionCode = promotionCode; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
