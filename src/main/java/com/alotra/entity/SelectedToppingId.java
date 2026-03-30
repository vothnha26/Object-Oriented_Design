package com.alotra.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SelectedToppingId implements Serializable {
    private Integer cartItemId; // renamed from ctghId
    private Integer toppingId;

    public SelectedToppingId() {}
    public SelectedToppingId(Integer cartItemId, Integer toppingId) {
        this.cartItemId = cartItemId;
        this.toppingId = toppingId;
    }

    public Integer getCartItemId() { return cartItemId; }
    public void setCartItemId(Integer cartItemId) { this.cartItemId = cartItemId; }
    public Integer getToppingId() { return toppingId; }
    public void setToppingId(Integer toppingId) { this.toppingId = toppingId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectedToppingId that)) return false;
        return Objects.equals(cartItemId, that.cartItemId) && Objects.equals(toppingId, that.toppingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartItemId, toppingId);
    }
}
