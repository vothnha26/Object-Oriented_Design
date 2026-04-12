# 🔴 Bài toán 5: Tính giá sản phẩm cứng nhắc (OCP Violation)

## Patterns: **Decorator** + **Strategy** (với Spring DI)
## SOLID vi phạm: **OCP** (Open/Closed), **SRP** (Single Responsibility)

---

## 📌 Vấn đề hiện tại

Giá sản phẩm trong đơn hàng được tính qua nhiều bước (giá gốc → topping → số lượng → khuyến mãi). Ban đầu, code sử dụng `if/else` hoặc `switch` trực tiếp trong Service để tính toán:

```java
// Trong PriceServiceImpl cũ
if (promo.getType() == PERCENTAGE) {
    total = total.multiply(rate);
} else if (promo.getType() == VALUE) {
    total = total.subtract(amount);
}
// Thêm loại KM mới (VD: Buy 1 Get 1) -> Phải sửa file này -> Vi phạm OCP
```

### ❌ Vấn đề cụ thể
1. **Vi phạm OCP**: Mỗi khi thêm loại khuyến mãi hoặc phí mới, phải sửa code logic tính tiền chính.
2. **Vi phạm SRP**: `PriceServiceImpl` vừa quản lý luồng tính tiền, vừa chi tiết hóa cách áp dụng từng loại giảm giá.
3. **Thiếu linh hoạt**: Khó thay đổi thứ tự áp dụng (VD: giảm giá trước hay sau khi cộng topping).

---

## ✅ Giải pháp: Decorator kết hợp Strategy (Spring Plugin)

Chúng ta sử dụng **Decorator Pattern** để chia nhỏ các bước tính toán thành từng lớp riêng biệt, và dùng **Strategy Pattern** (thông qua `PromotionApplicator`) để tự động chọn đúng Decorator mà không cần `if/else`.

### 1. Cấu trúc Decorator (Tính toán)
- **PriceComponent**: Interface gốc.
- **BasePrice**: Giá trị khởi đầu.
- **ToppingDecorator, QuantityDecorator**: Các lớp bọc tính toán cơ bản.
- **PromotionDecorator, ValueDiscountDecorator**: Các lớp bọc giảm giá.

### 2. Cấu trúc Strategy Applicator (Lắp ráp - OCP)
Chúng ta định nghĩa interface `PromotionApplicator`. Spring sẽ tự động thu thập tất cả các "mảnh ghép" này.

```java
public interface PromotionApplicator {
    boolean supports(PromotionType type);
    PriceComponent apply(PriceComponent base, Promotion promotion);
}
```

### 3. PriceServiceImpl (Điều phối sạch sẽ)
Service không còn quan tâm có bao nhiêu loại khuyến mãi, nó chỉ yêu cầu các "Applicator" làm việc:

```java
@Service
public class PriceServiceImpl implements PriceService {
    private final List<PromotionApplicator> applicators; // Tự động inject tất cả Strategy

    public void calculateTotal(Order order, String code) {
        PriceComponent chain = new BasePrice(subTotal);
        
        // Strategy Pattern tự động chọn đúng Decorator
        chain = applicators.stream()
                .filter(a -> a.supports(promo.getType()))
                .findFirst()
                .map(a -> a.apply(chain, promo))
                .orElse(chain);

        BigDecimal finalTotal = chain.calculate();
    }
}
```

---

## ✅ Lợi ích

| Đặc điểm | Trước (if/else) | Sau (Strategy + Decorator) |
|----------|-----------------|----------------------------|
| **Thêm loại KM mới** | Sửa file `PriceServiceImpl` | Tạo 1 class mới (Applicator + Decorator) |
| **Tính mở rộng** | Kém (vi phạm OCP) | Tuyệt vời (Plugin-based) |
| **Độ phức tạp** | Tăng dần theo số lượng `if` | Giữ nguyên, mỗi file 1 nhiệm vụ duy nhất |
| **Bảo trì** | Dễ gây bug tại code cũ | An toàn, không chạm vào code đang chạy |

---

## 📋 Sơ đồ Kiến trúc

Kiến trúc này biến hệ thống tính giá thành một **Pipeline** linh hoạt, nơi các quy tắc tính tiền có thể "cắm" thêm vào (Plug-in) bất cứ lúc nào.
