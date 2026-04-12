# 🔴 Bài toán 2: Xử lý thanh toán & tính giảm giá

## Patterns: **Strategy** + **Factory**
## SOLID vi phạm: **OCP**, **SRP**

---

## 📌 Vấn đề hiện tại

Logic xử lý cho từng phương thức thanh toán đang bị trộn lẫn trong Business Service bằng các câu lệnh `if/else`, gây khó khăn khi tích hợp thêm các cổng thanh toán mới như MoMo, VNPay.

### Code có vấn đề

```java
// Trong CheckoutService.java
public void processPayment(Order order, PaymentMethod method) {
    if (method == PaymentMethod.CASH) {
        // Logic cho COD
    } else if (method == PaymentMethod.BANK_TRANSFER) {
        // Logic xác thực chuyển khoản
    }
    // Càng nhiều phương thức, khối if/else càng khổng lồ
}
```

### ❌ Vấn đề cụ thể
1. **Vi phạm OCP**: Mỗi khi thêm một phương thức thanh toán mới, ta phải sửa đổi trực tiếp code trong Service.
2. **Khó bảo trì**: Logic xử lý của từng loại (ví dụ: gọi API MoMo vs kiểm tra tiền mặt) quá khác biệt nhưng lại nằm chung một chỗ.

---

## ✅ Giải pháp: Strategy Pattern

Mỗi phương thức thanh toán được đóng gói trong một lớp Strategy riêng biệt.

```java
// ===== Strategy Interface =====
public interface PaymentStrategy {
    void process(Order order);
}

// ===== Concrete Strategies =====
public class CashPaymentStrategy implements PaymentStrategy {
    @Override public void process(Order order) { /* Logic tiền mặt */ }
}

public class BankTransferPaymentStrategy implements PaymentStrategy {
    @Override public void process(Order order) { /* Logic chuyển khoản */ }
}
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| if/else rải rác | Mỗi phương thức là một class độc lập |
| Thêm cổng mới → sửa code cũ | Thêm class mới mà không chạm code cũ (OCP) |
| Logic trộn lẫn | Tách biệt hoàn toàn logic thanh toán |
