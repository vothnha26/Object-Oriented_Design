package com.alotra.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartItemDTO {
    private Integer variantId;
    private Integer quantity;
    private String variantName;
    private String sizeName;
    private BigDecimal price;
    private String note;
    private List<Integer> toppingIds;

    // Getters and Setters
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public List<Integer> getToppingIds() { return toppingIds; }
    public void setToppingIds(List<Integer> toppingIds) { this.toppingIds = toppingIds; }
}
