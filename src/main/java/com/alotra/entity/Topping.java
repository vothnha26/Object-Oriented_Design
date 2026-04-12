package com.alotra.entity;

import com.alotra.entity.enums.ToppingStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "toppings")
public class Topping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "extra_price", nullable = false)
    private BigDecimal extraPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ToppingStatus status = ToppingStatus.ACTIVE;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;

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
    public java.time.LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(java.time.LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
