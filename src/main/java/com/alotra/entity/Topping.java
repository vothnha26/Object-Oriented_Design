package com.alotra.entity;

import com.alotra.entity.enums.ToppingStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Topping")
public class Topping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTopping")
    private Integer id;

    @Column(name = "TenTopping", nullable = false, unique = true)
    private String name;

    @Column(name = "GiaThem", nullable = false)
    private BigDecimal extraPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai", nullable = false)
    private ToppingStatus status = ToppingStatus.AVAILABLE;

    @Column(name = "UrlAnh")
    private String imageUrl;

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getExtraPrice() { return extraPrice; }
    public void setExtraPrice(BigDecimal extraPrice) { this.extraPrice = extraPrice; }
    public ToppingStatus getStatus() { return status; }
    public void setStatus(ToppingStatus status) { this.status = status; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    // Business methods
    public boolean isAvailable() { return status == ToppingStatus.AVAILABLE && deletedAt == null; }
}