package com.alotra.entity;

import com.alotra.entity.enums.PromotionStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String code;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.alotra.entity.enums.PromotionType type = com.alotra.entity.enums.PromotionType.VALUE;

    @Column(name = "discount_value")
    private java.math.BigDecimal discountValue = java.math.BigDecimal.ZERO;

    @Column(name = "discount_rate")
    private Integer discountRate = 0; // Dùng cho phần trăm (VD: 10 cho 10%)

    @Column(name = "min_order_amount")
    private java.math.BigDecimal minOrderAmount = java.math.BigDecimal.ZERO;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionStatus status = PromotionStatus.ACTIVE;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_public")
    private boolean isPublic = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isActive() {
        return status == PromotionStatus.ACTIVE
                && deletedAt == null
                && (endDate == null || !endDate.isBefore(LocalDate.now()))
                && (usageLimit == null || usedCount < usageLimit);
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public com.alotra.entity.enums.PromotionType getType() { return type; }
    public void setType(com.alotra.entity.enums.PromotionType type) { this.type = type; }
    public java.math.BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(java.math.BigDecimal discountValue) { this.discountValue = discountValue; }
    public Integer getDiscountRate() { return discountRate; }
    public void setDiscountRate(Integer discountRate) { this.discountRate = discountRate; }
    public java.math.BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(java.math.BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
    public PromotionStatus getStatus() { return status; }
    public void setStatus(PromotionStatus status) { this.status = status; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
