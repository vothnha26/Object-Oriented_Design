# 🔴 Bài toán 2: Xử lý thanh toán & tính giảm giá

## Patterns: **Strategy** + **Factory**
## SOLID vi phạm: **OCP**, **DIP**, **SRP**

---

## 📌 Vấn đề hiện tại

### Vấn đề A: Logic thanh toán dựa trên String

Phương thức thanh toán chỉ là một **String field** (`paymentMethod = PaymentMethod.BANK_TRANSFER | PaymentMethod.CASH`). Khi cần thêm logic riêng cho mỗi phương thức (VD: validate chuyển khoản cần xác nhận, COD tự hoàn tất), code dùng `if/else`:

**`ShipperOrderService.java`**:
```java
// Block nếu chưa thanh toán mà là chuyển khoản
if (PaymentMethod.BANK_TRANSFER.equalsIgnoreCase(order.getPaymentMethod())
        && !PaymentStatus.PAID.equals(order.getPaymentStatus())) {
    return false;
}
```

Nếu thêm phương thức mới (MoMo, ZaloPay, VNPay,...) → phải sửa tất cả `if/else` → **vi phạm OCP**.

### Vấn đề B: Logic tính giảm giá bị duplicate

Hàm `applyPercent()` bị **copy-paste** giữa 2 service:

**`OrderService.java`** (dòng 113-120):
```java
private BigDecimal applyPercent(BigDecimal base, Integer percent) {
    if (base == null) return null;
    if (percent == null || percent <= 0) return base;
    java.math.RoundingMode RM = java.math.RoundingMode.HALF_UP;
    java.math.BigDecimal p = java.math.BigDecimal.valueOf(100 - Math.min(100, percent))
            .divide(java.math.BigDecimal.valueOf(100), 4, RM);
    return base.multiply(p).setScale(0, RM);
}
```

**`ProductService.java`** (dòng 111-116) — **gần giống hệt**:
```java
private BigDecimal applyPercent(BigDecimal base, Integer percent) {
    if (base == null) return null;
    if (percent == null || percent <= 0) return base;
    BigDecimal p = BigDecimal.valueOf(100 - Math.min(100, percent))
            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    return base.multiply(p).setScale(0, RoundingMode.HALF_UP);
}
```

→ **Vi phạm DRY** (Don't Repeat Yourself) và **SRP** (service kiêm tính toán giá).

---

## ✅ Giải pháp

### Strategy Pattern cho thanh toán

```java
// ===== Strategy Interface =====
public interface PaymentStrategy {
    PaymentMethod getMethod();
    boolean requiresPrePayment();     // cần thanh toán trước khi giao?
    boolean validatePayment(Order order);
    void processPayment(Order order);
}

// ===== Concrete Strategies =====
public class CashPaymentStrategy implements PaymentStrategy {
    @Override public PaymentMethod getMethod() { return PaymentMethod.CASH; }
    @Override public boolean requiresPrePayment() { return false; }
    @Override public boolean validatePayment(Order order) { return true; }
    @Override public void processPayment(Order order) {
        // COD: thanh toán khi nhận hàng -> chỉ cần đánh dấu trạng thái khi giao xong
    }
}

public class BankTransferPaymentStrategy implements PaymentStrategy {
    @Override public PaymentMethod getMethod() { return PaymentMethod.BANK_TRANSFER; }
    @Override public boolean requiresPrePayment() { return true; }
    @Override public boolean validatePayment(Order order) {
        return order.getPayment() != null && PaymentStatus.PAID.equals(order.getPayment().getStatus());
    }
    @Override public void processPayment(Order order) {
        // Kiểm tra webhook xác nhận chuyển khoản
    }
}

// Future: MoMoPaymentStrategy, ...
```

### Factory Pattern để tạo Strategy đúng:

```java
public class PaymentStrategyFactory {

    private static final Map<PaymentMethod, Supplier<PaymentStrategy>> registry = Map.of(
        PaymentMethod.CASH, CashPaymentStrategy::new,
        PaymentMethod.BANK_TRANSFER, BankTransferPaymentStrategy::new
    );

    public static PaymentStrategy create(PaymentMethod method) {
        Supplier<PaymentStrategy> supplier = registry.get(method);
        if (supplier == null) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ: " + method);
        }
        return supplier.get();
    }
}
```

### Strategy cho tính giảm giá (loại bỏ duplicate):

```java
public interface DiscountStrategy {
    BigDecimal apply(BigDecimal basePrice);
}

public class PercentDiscountStrategy implements DiscountStrategy {
    private final int percent;

    public PercentDiscountStrategy(int percent) {
        this.percent = Math.min(100, Math.max(0, percent));
    }

    @Override
    public BigDecimal apply(BigDecimal basePrice) {
        if (basePrice == null || percent <= 0) return basePrice;
        BigDecimal factor = BigDecimal.valueOf(100 - percent)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return basePrice.multiply(factor).setScale(0, RoundingMode.HALF_UP);
    }
}

public class NoDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal apply(BigDecimal basePrice) {
        return basePrice;
    }
}
```

### Sử dụng trong service (loại bỏ duplicate):

```java
// Trước (duplicate)
BigDecimal finalPrice = applyPercent(base, percent); // copy-paste trong cả 2 service

// Sau (DRY + DIP)
DiscountStrategy discount = percent != null && percent > 0
    ? new PercentDiscountStrategy(percent)
    : new NoDiscountStrategy();
BigDecimal finalPrice = discount.apply(base);
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| if/else rải rác khi check payment | Mỗi phương thức thanh toán = 1 class |
| Thêm MoMo → sửa 5 chỗ | Thêm MoMo → 1 class + đăng ký vào factory |
| `applyPercent()` copy 2 lần | 1 `DiscountStrategy` dùng chung |
| Service phụ thuộc vào String/Enum | Service phụ thuộc vào abstraction |
