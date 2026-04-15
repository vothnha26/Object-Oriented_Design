package com.alotra.dto;

import java.util.List;

public class CheckoutRequest {
    private String addressLine;
    private String paymentMethod;
    private String promotionCode;
    private String note;
    private List<CartItemDTO> cartItems;

    // Getters and Setters
    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPromotionCode() { return promotionCode; }
    public void setPromotionCode(String promotionCode) { this.promotionCode = promotionCode; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public List<CartItemDTO> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItemDTO> cartItems) { this.cartItems = cartItems; }
}
