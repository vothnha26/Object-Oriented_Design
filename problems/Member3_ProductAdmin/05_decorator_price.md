# 🔴 Bài toán 5: Hệ thống tính giá phức tạp và cứng nhắc (OCP Violation)

## Patterns: **Decorator** + **Strategy** + **Chain of Responsibility (Pipeline)**
## SOLID vi phạm: **OCP** (Open/Closed), **SRP** (Single Responsibility)

---

## 📌 Vấn đề hiện tại

Giá sản phẩm và đơn hàng trong hệ thống Thương mại điện tử không chỉ là phép cộng đơn giản. Nó trải qua một chuỗi các bước biến đổi dữ liệu:
`Giá gốc sản phẩm` -> `Cộng Toppings` -> `Nhân số lượng` -> `Áp dụng Khuyến mãi` -> `Cộng phí vận chuyển` -> `Làm tròn`.

### ❌ Vấn đề cụ thể
1. **Vi phạm OCP**: Ban đầu, `PriceServiceImpl` chứa một chuỗi logic "cứng" (Hard-coded). Mỗi khi thêm một loại phí mới (VAT, Phí dịch vụ, Phí đóng gói...), lập trình viên buộc phải mở file Service ra để sửa đổi.
2. **Vi phạm SRP**: Service phải gánh vác quá nhiều trách nhiệm: vừa tính giá item, vừa tìm khuyến mãi, vừa cộng phí ship, vừa làm tròn.
3. **Khó kiểm soát thứ tự**: Rất khó để thay đổi thứ tự áp dụng (ví dụ: Giảm giá trước hay sau khi cộng phí vận chuyển).

---

## ✅ Giải pháp: Price Pipeline (Kiến trúc Đa tầng)

Chúng ta nâng cấp hệ thống thành một **Dây chuyền sản xuất tự động (Pipeline)**. Sự kết hợp của 3 Pattern giúp hệ thống đạt độ linh hoạt tối thượng:

### 1. Decorator Pattern (Cốt lõi tính toán - "Củ hành")
Sử dụng để "bọc" các lớp tính toán chồng lên nhau.
- **Root**: `PriceComponent`.
- **Implementations**: `BasePrice`, `ToppingDecorator`, `QuantityDecorator`, `PercentagePromotionDecorator`, `ValuePromotionDecorator`, `ShippingDecorator`, `RoundingDecorator`.

### 2. Strategy Pattern (Lựa chọn khuyến mãi)
Sử dụng `PromotionApplicator` để tự động chọn đúng loại Decorator giảm giá dựa trên dữ liệu từ Database mà không dùng `if/else`.

### 3. Chain of Responsibility Pattern (Điều phối Pipeline)
Đây là "xương sống" của hệ thống mới. Chúng ta định nghĩa các "Trạm xử lý" (`OrderPriceProcessor`).

```java
public interface OrderPriceProcessor {
    int getOrder(); // Thứ tự ưu tiên (10, 20, 30...)
    PriceComponent process(PriceComponent chain, Order order, String promoCode);
}
```

---

## ✅ Cấu trúc mã nguồn (Sub-packages)

Để dễ quản lý, mã nguồn được tổ chức thành các thư mục con chuyên biệt trong `com.alotra.service.pricing`:
- `core`: (Root package) `PriceComponent`, `PriceDecorator`, `OrderPriceProcessor`, `PromotionApplicator`.
- `component`: `BasePrice` (Điểm bắt đầu).
- `decorator`: Các lớp bọc tính toán (`Topping`, `Quantity`, `Shipping`, `Rounding`...).
- `strategy`: Các chiến lược áp dụng khuyến mãi (`Percentage`, `Value`).
- `processor`: Các trạm trong đường ống Pipeline (`ItemSubtotal`, `Promotion`, `Shipping`, `Rounding`).

---

## ✅ PriceServiceImpl (Điều phối tối giản)

Mã nguồn Service giờ đây cực kỳ sạch sẽ, nó chỉ việc đẩy Đơn hàng vào đường ống:

```java
@Service
public class PriceServiceImpl implements PriceService {
    private final List<OrderPriceProcessor> processors; // Tự động nạp tất cả các trạm

    public PriceServiceImpl(List<OrderPriceProcessor> processors) {
        // Sắp xếp theo getOrder()
        this.processors = processors.stream()
                .sorted(Comparator.comparingInt(OrderPriceProcessor::getOrder))
                .collect(Collectors.toList());
    }

    public void calculateTotal(Order order, String code) {
        PriceComponent chain = null;
        for (OrderPriceProcessor processor : processors) {
            chain = processor.process(chain, order, code);
        }
        order.setTotalAmount(chain.calculate());
    }
}
```

---

## ✅ Lợi ích vượt trội

| Đặc điểm | Trước (Manual Decorator) | Sau (Price Pipeline) |
|----------|--------------------------|----------------------|
| **Thêm phí mới (VAT, Fee)** | Sửa file `PriceServiceImpl` | **Chỉ tạo 1 class Processor mới** |
| **Thay đổi thứ tự** | Sửa code điều phối | **Chỉ sửa giá trị `getOrder()`** |
| **Tính tự động** | Thấp | Tuyệt vời (Plugin-based via Spring) |
| **Bảo trì** | Nguy cơ gây lỗi vùng code cũ | An toàn tuyệt đối theo nguyên tắc OCP |

---

## 📋 Sơ đồ Kiến trúc

Kiến trúc này biến hệ thống tính giá thành một **Pipeline** linh hoạt, nơi các quy tắc kinh doanh có thể "cắm" thêm vào (Plug-in) bất cứ lúc nào mà không làm ảnh hưởng đến lõi hệ thống.
