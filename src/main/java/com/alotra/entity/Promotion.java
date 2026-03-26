package com.alotra.entity;

import com.alotra.entity.enums.PromotionStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "Promotion")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaKM")
    private Integer id;

    @Column(name = "TenSuKien", nullable = false)
    private String name;

    @Column(name = "MoTa")
    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "NgayBD", nullable = false)
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "NgayKT", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai", nullable = false)
    private PromotionStatus status = PromotionStatus.ACTIVE;

    @Column(name = "UrlAnh")
    private String imageUrl;

    @Column(name = "LuotXem")
    private Integer views;

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;

    public boolean isActive() {
        return status == PromotionStatus.ACTIVE
                && deletedAt == null
                && (endDate == null || !endDate.isBefore(LocalDate.now()));
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public PromotionStatus getStatus() { return status; }
    public void setStatus(PromotionStatus status) { this.status = status; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getViews() { return views; }
    public void setViews(Integer views) { this.views = views; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
