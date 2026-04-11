package com.alotra.dto;

import java.util.List;

public class CartItemDTO {
    private Integer variantId;
    private Integer quantity;
    private List<Integer> toppingIds;

    // Getters and Setters
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public List<Integer> getToppingIds() { return toppingIds; }
    public void setToppingIds(List<Integer> toppingIds) { this.toppingIds = toppingIds; }
}
