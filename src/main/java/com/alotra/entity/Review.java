package com.alotra.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Review",
       uniqueConstraints = @UniqueConstraint(name = "UQ_Review_OnePerLine", columnNames = {"MaKH", "MaCT"}))
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDG")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaKH", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaCT", nullable = false)
    private OrderItem orderLine;

    @Column(name = "SoSao", nullable = false)
    private Integer stars;

    @Column(name = "BinhLuan")
    private String comment;

    @Column(name = "NgayDG", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "TraLoiAdmin")
    private String adminReply;

    @Column(name = "TraLoiLuc")
    private LocalDateTime adminRepliedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TraLoiBoi")
    private Employee repliedBy;

    public boolean hasReply() {
        return adminReply != null && !adminReply.isBlank();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public OrderItem getOrderLine() { return orderLine; }
    public void setOrderLine(OrderItem orderLine) { this.orderLine = orderLine; }
    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }
    public LocalDateTime getAdminRepliedAt() { return adminRepliedAt; }
    public void setAdminRepliedAt(LocalDateTime adminRepliedAt) { this.adminRepliedAt = adminRepliedAt; }
    public Employee getRepliedBy() { return repliedBy; }
    public void setRepliedBy(Employee repliedBy) { this.repliedBy = repliedBy; }
}
