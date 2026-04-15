package com.alotra.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_detail_id")
    private PromotionDetail detail;

    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return (endDate == null || !endDate.isBefore(now))
                && (startDate == null || !startDate.isAfter(now));
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (detail == null) return BigDecimal.ZERO;
        
        // Kiểm tra điều kiện giá trị đơn hàng tối thiểu từ PromotionDetail
        if (detail.getMinOrderAmount() != null && orderAmount.compareTo(detail.getMinOrderAmount()) < 0) {
            return BigDecimal.ZERO;
        }
        
        return detail.calculate(orderAmount);
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
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public PromotionDetail getDetail() { return detail; }
    public void setDetail(PromotionDetail detail) { this.detail = detail; }
}
