package com.alotra.entity;

import com.alotra.entity.enums.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDH")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaKH", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNV")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaKM")
    private Promotion promotion;

    @Column(name = "NgayLap", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThaiDonHang", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Embedded
    private PaymentInfo payment = new PaymentInfo();

    @Column(name = "TongHang", nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "GiamGiaDon", nullable = false)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "PhiVanChuyen", nullable = false)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "TongThanhToan", nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "GhiChu")
    private String note;

    @Embedded
    private ShippingInfo shippingInfo;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // === Business methods ===
    public boolean canCancel() { return status == OrderStatus.PENDING; }
    public boolean isDone() { return status == OrderStatus.DELIVERED; }
    
    /**
     * Tính toán tổng tiền thực tế dựa trên các thành phần giá.
     * Đây là logic nghiệp vụ, totalAmount sẽ được gán bằng giá trị này trước khi lưu.
     */
    public BigDecimal calcFinalTotal() { 
        return subtotal.subtract(discount).add(shippingFee); 
    }

    public void syncTotal() {
        this.totalAmount = calcFinalTotal();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public Promotion getPromotion() { return promotion; }
    public void setPromotion(Promotion promotion) { this.promotion = promotion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public PaymentInfo getPayment() { return payment; }
    public void setPayment(PaymentInfo payment) { this.payment = payment; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public ShippingInfo getShippingInfo() { return shippingInfo; }
    public void setShippingInfo(ShippingInfo shippingInfo) { this.shippingInfo = shippingInfo; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
