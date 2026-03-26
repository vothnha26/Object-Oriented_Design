package com.alotra.entity;

import com.alotra.entity.enums.ProductStatus;
import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(name = "ProductVariant")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductSizeId", nullable = false)
    private ProductSize size;

    @Column(name = "Price", nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    // === Business methods ===
    public boolean isActive() { return status == ProductStatus.ACTIVE; }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public ProductSize getSize() { return size; }
    public void setSize(ProductSize size) { this.size = size; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
}