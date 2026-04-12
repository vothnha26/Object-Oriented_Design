# 🔴 Bài toán 1: Quản lý trạng thái đơn hàng

## Patterns: **State** + **Command**
## SOLID vi phạm: **OCP** (Open/Closed), **SRP** (Single Responsibility)

---

## 📌 Vấn đề hiện tại

Trạng thái đơn hàng (`Order.status`) được quản lý bằng các câu lệnh `if/switch` nằm rải rác trong nhiều service, gây khó khăn khi muốn thêm trạng thái mới.

### Code có vấn đề

**`VendorOrderService.java`** — dùng `switch` để tính trạng thái tiếp theo:
```java
public OrderStatus nextStatus(OrderStatus current) {
    if (current == null) return OrderStatus.PENDING;
    return switch (current) {
        case OrderStatus.PENDING -> OrderStatus.PREPARING;
        case OrderStatus.PREPARING -> OrderStatus.DELIVERING;
        case OrderStatus.DELIVERING -> OrderStatus.DELIVERED;
        default -> current;
    };
}

public boolean canCancel(OrderStatus current) {
    return current.equals(OrderStatus.PENDING) || current.equals(OrderStatus.PREPARING);
}
```

**`ReviewService.java`** — kiểm tra trạng thái bằng hardcode logic:
```java
public boolean isOrderEligibleForReview(OrderStatus orderStatus, PaymentStatus paymentStatus) {
    return OrderStatus.DELIVERED.equals(orderStatus) && PaymentStatus.PAID.equals(paymentStatus);
}
```

### ⚠️ Vấn đề cụ thể

1. **Vi phạm OCP**: Thêm trạng thái mới (VD: "Đang đổi trả") → phải tìm và sửa tất cả các khối `switch/if` ở nhiều file.
2. **Vi phạm SRP**: Business Service phải gánh thêm logic chuyển đổi trạng thái phức tạp.
3. **Thiếu validation**: Khó kiểm soát việc chuyển trạng thái bất hợp lệ (VD: Đã giao → Chờ xử lý).

---

## ✅ Giải pháp: State Pattern + Command Pattern

### State Pattern
Mỗi trạng thái đơn hàng là một **class riêng** implement interface `OrderState`. Đơn hàng giữ tham chiếu tới trạng thái hiện tại và ủy thác hành vi cho class đó.

### Thiết kế mới

```java
// ===== Interface =====
public interface OrderState {
    void advance(OrderContext context);    // Chuyển sang trạng thái tiếp theo
    void cancel(OrderContext context);     // Hủy đơn hàng
    boolean canCancel();
    boolean canReview();
    String getStatusName();
}

// ===== Concrete States =====
public class PendingState implements OrderState {
    @Override
    public void advance(OrderContext ctx) { ctx.setState(new PreparingState()); }
    @Override
    public void cancel(OrderContext ctx) { ctx.setState(new CancelledState()); }
    @Override public boolean canCancel() { return true; }
    @Override public boolean canReview() { return false; }
    @Override public String getStatusName() { return "PENDING"; }
}

// ... các trạng thái khác tương tự
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| Logic switch-case rải rác | Tập trung logic vào từng lớp State |
| Khó mở rộng trạng thái | Chỉ cần tạo thêm lớp State mới (OCP) |
| Dễ lỗi logic chuyển đổi | Quy tắc chuyển trạng thái được đóng gói minh bạch |
