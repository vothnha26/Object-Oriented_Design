# 🔴 Bài toán 6: CheckoutService là God Class (Web-layer Cart)

## Pattern: **Facade**
## SOLID vi phạm: **SRP** (Single Responsibility), **DIP** (Dependency Inversion)

---

## 📌 Vấn đề hiện tại

Khi hệ thống chuyển từ quản lý Giỏ hàng (`Cart`) phía client (localStorage) sang phía Web-layer (Session/Cookie), `CheckoutService` cũ phải gồng gánh quá nhiều trách nhiệm:
1. Chuyển đổi dữ liệu từ Web-layer (DTO) sang Entity.
2. Kiểm tra tồn kho (Stock).
3. Tính toán giá và khuyến mãi (Pricing & Promotion).
4. Xử lý thanh toán (Payment).
5. Lưu trữ vào cơ sở dữ liệu.

Điều này biến `CheckoutService` thành một **God Class**, rất khó bảo trì và mở rộng khi logic khuyến mãi hoặc thanh toán thay đổi.

---

## ✅ Giải pháp: Facade Pattern

Chúng ta tách `CheckoutService` thành các service chuyên biệt và sử dụng **`CheckoutFacade`** làm điểm truy cập duy nhất cho Controller. Facade sẽ điều phối luồng thực thi:

### Cập nhật cấu trúc:
- **CheckoutFacade**: Điều phối các service.
- **OrderFactory**: Chuyển đổi `CheckoutRequest` (DTO) từ Web-layer thành `Order` (Entity), tích hợp **OrderBuilder** (Member 1).
- **StockService**: Kiểm tra tồn hàng.
- **PriceService**: Tính toán giá và áp dụng Promotion (thuộc trách nhiệm Member 2).
- **PaymentService**: Xử lý cổng thanh toán.

```java
@Service
public class CheckoutFacade {
    @Transactional
    public Order processCheckout(Customer customer, CheckoutRequest request) {
        // 1. Kiểm tra tồn kho
        stockService.validateStock(request.getCartItems());

        // 2. Tạo Order từ DTO (Sử dụng Factory + Builder)
        Order order = orderFactory.createOrder(customer, address, request.getCartItems());

        // 3. Tính toán giá
        priceService.calculateTotal(order, request.getPromotionCode());

        // 4. Xử lý thanh toán
        paymentService.processPayment(order, request.getPaymentMethod());

        // 5. Lưu đơn hàng
        return checkoutService.saveOrder(order);
    }
}
```

## ✅ Lợi ích
1. **Tuân thủ SRP**: Mỗi service chỉ làm một việc duy nhất. `CheckoutService` giờ chỉ còn nhiệm vụ lưu trữ.
2. **Dễ dàng mở rộng**: Có thể thay đổi `PaymentServiceImpl` hoặc `PriceServiceImpl` mà không ảnh hưởng tới luồng checkout chính.
3. **Giảm phụ thuộc**: Controller chỉ cần biết đến `CheckoutFacade`, không cần inject 10+ dependencies khác nhau.
