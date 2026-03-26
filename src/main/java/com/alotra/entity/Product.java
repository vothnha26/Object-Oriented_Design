package com.alotra.entity;

import com.alotra.entity.enums.ProductStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSP")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDM", nullable = false)
    private Category category;

    @Column(name = "TenSP", nullable = false)
    private String name;

    @Column(name = "MoTa")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai", nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "UrlAnh")
    private String imageUrl;

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;

    // === Business methods ===
    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE && deletedAt == null;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}