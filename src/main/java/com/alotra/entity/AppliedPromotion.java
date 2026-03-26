package com.alotra.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "AppliedPromotion")
public class AppliedPromotion {
    @EmbeddedId
    private AppliedPromotionId id = new AppliedPromotionId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("promotionId")
    @JoinColumn(name = "MaKM")
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSP")
    @MapsId("productId")
    private Product product;

    @Column(name = "PhanTramGiam", nullable = false)
    private Integer discountPercent;

    public AppliedPromotionId getId() { return id; }
    public void setId(AppliedPromotionId id) { this.id = id; }
    public Promotion getPromotion() { return promotion; }
    public void setPromotion(Promotion promotion) { this.promotion = promotion; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }
}
