# 🔴 Bài toán 13: Logic tính giá phức tạp & Gắn chặt vào Entity

## Patterns: **Decorator** + **Strategy**
## SOLID vi phạm: **SRP**, **OCP**, **DRY**

---

## 📌 Vấn đề hiện tại

Trước đây, thực thể `Order` (Entity) phải tự gánh vác toàn bộ logic tính toán tài chính phức tạp. Khi có thêm các yêu cầu mới như phí vận chuyển, thuế, hoặc nhiều loại khuyến mãi cùng lúc, code trong `Order` trở nên khổng lồ và khó kiểm soát.

### Code có vấn đề (Trong `Order.java` cũ)
```java
public BigDecimal calculateFinalTotal() {
    BigDecimal subTotal = calculateSubTotal();
    BigDecimal discount = BigDecimal.ZERO;
    
    if (promotion != null) {
        discount = promotion.calculateDiscount(subTotal);
    }
    
    // Nếu thêm phí ship, thuế VAT... phải sửa trực tiếp ở đây
    BigDecimal finalTotal = subTotal.subtract(discount);
    return finalTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalTotal;
}
```

### ❌ Vấn đề cụ thể
1. **Vi phạm SRP**: `Order` nên là một "vật chứa dữ liệu" (Data Holder), không nên chứa logic nghiệp vụ tính toán thuế/phí/khuyến mãi phức tạp.
2. **Vi phạm OCP**: Mỗi khi hệ thống thêm một loại phí mới (VD: Phí đóng gói), ta phải mở file `Order.java` ra để sửa.
3. **Logic cứng nhắc**: Rất khó để thay đổi thứ tự ưu tiên tính toán (VD: Giảm giá trước hay tính thuế trước?).

---

## ✅ Giải pháp: Decorator + Strategy + Delegation

Hệ thống đã được refactor để tách biệt hoàn toàn **"Dữ liệu đơn hàng"** và **"Cơ chế tính giá"**.

### 1. Bộ khung Decorator (Structural)
Tạo ra các lớp "bọc" lấy nhau để tính toán giá trị theo chuỗi (Pipeline).
- **`PriceComponent`**: Interface chung.
- **`BasePrice`**: Thành phần cơ bản (giá gốc).
- **`PriceDecorator`**: Lớp trừu tượng cho các thành phần bổ sung.
- **`PromotionDecorator`**: Bọc giá gốc và áp dụng giảm giá.

### 2. Kết hợp Strategy (Behavioral)
`PromotionDecorator` không tự tính giảm giá mà giao phó cho `DiscountStrategy`. Điều này cho phép thay đổi thuật toán giảm giá (Phần trăm, Số tiền cố định, Đồng giá) mà không đổi cấu trúc Decorator.

### 3. Giao phó cho Service (Delegation)
Toàn bộ việc xây dựng "chuỗi" Decorator được đẩy ra `PricingService`. Entity `Order` hiện tại cực kỳ mỏng.

---

## 🛠 Các file và Vai trò sau Refactor

#### `src/main/java/com/alotra/service/pricing/PricingService.java`
- **Vai trò**: "Tổng quản" tính giá.
- **Giải quyết**: Xây dựng Pipeline Decorator (Base -> Promotion -> ...) và trả về kết quả cuối cùng.

#### `src/main/java/com/alotra/entity/Order.java`
- **Vai trò**: Entity thuần túy (Pure Data Holder).
- **Giải quyết**: Loại bỏ hoàn toàn các method tính toán, kể cả `getSubTotal`. Thực thể giờ chỉ đóng vai trò cung cấp dữ liệu thô (danh sách items, mã khuyến mãi).

#### `src/main/java/com/alotra/service/pricing/PromotionDecorator.java`
- **Vai trò**: Một mắt xích trong chuỗi tính giá.
- **Giải quyết**: Lấy giá từ mắt xích trước đó, áp dụng Strategy giảm giá và trả về kết quả.

#### `src/main/java/com/alotra/builder/OrderBuilder.java`
- **Vai trò**: Dựng đối tượng Order.
- **Giải quyết**: Không còn tự ý tính giá và gán vào Payment. Việc này được bàn giao cho Service layer để đảm bảo tính nhất quán.

---

## 🚀 Lợi ích của thiết kế mới

| Đặc điểm | Trước Refactor | Sau Refactor |
|-------|-----|-----|
| **Mở rộng** | Sửa code trong Entity (Nguy hiểm) | Tạo Decorator mới (An toàn - OCP) |
| **Trách nhiệm** | Order làm quá nhiều việc | `PricingService` chuyên trách tính toán (SRP) |
| **Linh hoạt** | Thứ tự tính toán cố định | Thay đổi thứ tự bọc Decorator = Thay đổi thứ tự tính |
| **Kiểm thử** | Phải test cả Entity Order | Unit Test từng Decorator cực kỳ dễ dàng |

---

## 🔄 Luồng thực thi mới

1. `CheckoutFacade` nhận yêu cầu thanh toán.
2. Gọi `PriceService` để xử lý tài chính.
3. `PriceService` gọi `PricingService.calculateFinalTotal(order)`.
4. `PricingService` dựng chuỗi: `BasePrice` -> `PromotionDecorator` -> `calculate()`.
5. Kết quả được lưu vào bản ghi `Payment` để hoàn tất đơn hàng.
