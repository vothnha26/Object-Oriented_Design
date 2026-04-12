# 🔴 Bài toán 4: Thiếu cơ chế thông báo khi đơn hàng thay đổi

## Pattern: **Observer**
## SOLID vi phạm: **OCP**, **SRP**

---

## 📌 Vấn đề hiện tại

Khi trạng thái đơn hàng thay đổi, hệ thống không tự động thông báo cho khách hàng hoặc cập nhật các hệ thống liên quan.

### Code có vấn đề

**`VendorOrderService.java`** — chỉ cập nhật DB:
```java
public void updateStatus(Integer id, OrderStatus newStatus) {
    Order order = orderRepository.findById(id).orElseThrow();
    order.setStatus(newStatus);
    orderRepository.save(order);
    // Kết thúc — không có email hay thông báo nào được gửi đi
}
```

### ❌ Vấn đề cụ thể
1. **Trải nghiệm kém**: Khách hàng không nhận được thông tin về tiến độ đơn hàng.
2. **Khó mở rộng**: Nếu muốn thêm tính năng gửi SMS hoặc cập nhật kho khi đơn hoàn tất, ta phải sửa trực tiếp code nghiệp vụ → vi phạm OCP.
3. **Vi phạm SRP**: Service xử lý đơn hàng không nên kiêm nhiệm cả việc gửi email hay thông báo.

---

## ✅ Giải pháp: Observer Pattern

Sử dụng cơ chế sự kiện (Event-driven) để tách biệt logic nghiệp vụ đơn hàng và logic thông báo.

```java
// ===== Observer Interface =====
public interface OrderEventListener {
    void onOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus);
}

// ===== Concrete Observers =====
@Component
public class EmailNotifier implements OrderEventListener {
    @Override
    public void onOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        // Logic gửi email cho khách hàng
    }
}

@Component
public class ActivityLogger implements OrderEventListener {
    @Override
    public void onOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        log.info("Order #{} changed from {} to {}", order.getId(), oldStatus, newStatus);
    }
}
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| Không thông báo gì | Tự động thông báo qua nhiều kênh |
| Thêm thông báo mới → sửa service | Thêm Observer mới mà không chạm vào code cũ |
| Service gánh quá nhiều trách nhiệm | Service chỉ cần phát sự kiện |
