package com.alotra.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AppliedPromotionId implements Serializable {
    private Integer promotionId;
    private Integer productId;

    public AppliedPromotionId() {}
    public AppliedPromotionId(Integer promotionId, Integer productId) {
        this.promotionId = promotionId;
        this.productId = productId;
    }
    public Integer getPromotionId() { return promotionId; }
    public void setPromotionId(Integer promotionId) { this.promotionId = promotionId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppliedPromotionId that)) return false;
        return Objects.equals(promotionId, that.promotionId) && Objects.equals(productId, that.productId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(promotionId, productId);
    }
}
