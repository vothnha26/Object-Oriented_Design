package com.alotra.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OrderedToppingId implements Serializable {
    private Integer orderItemId; // renamed from ctId
    private Integer toppingId;

    public OrderedToppingId() {}
    public OrderedToppingId(Integer orderItemId, Integer toppingId) {
        this.orderItemId = orderItemId;
        this.toppingId = toppingId;
    }

    public Integer getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Integer orderItemId) { this.orderItemId = orderItemId; }
    public Integer getToppingId() { return toppingId; }
    public void setToppingId(Integer toppingId) { this.toppingId = toppingId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderedToppingId that)) return false;
        return Objects.equals(orderItemId, that.orderItemId) && Objects.equals(toppingId, that.toppingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderItemId, toppingId);
    }
}
