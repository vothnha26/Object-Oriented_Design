# 🔴 Bài toán 3: Tạo đơn hàng quá phức tạp

## Pattern: **Builder**
## SOLID vi phạm: **SRP**

---

## 📌 Vấn đề hiện tại

Method `processCheckout()` trong `CheckoutService.java` nhận quá nhiều tham số và chứa logic xây dựng đối tượng đơn hàng cồng kềnh, khiến code khó đọc và khó bảo trì.

```java
public Order processCheckout(
    Customer kh,
    List<Integer> itemIds,
    PaymentMethod paymentMethod,
    String note,
    String receivingMethod,
    String shipAddress
) {
    // 90+ dòng logic: validate → build Order → build LineItems...
}
```

### ⚠️ Vấn đề cụ thể
1. **Quá nhiều tham số**: Dễ nhầm lẫn thứ tự các tham số khi gọi hàm.
2. **Logic xây dựng phức tạp**: Việc thiết lập nhiều thuộc tính cho `Order` (địa chỉ, giảm giá, trạng thái...) làm loãng logic nghiệp vụ chính.
3. **Khó kiểm soát địa chỉ**: Địa chỉ cần được lưu dưới dạng chuỗi (Snapshot) tại thời điểm đặt hàng để đảm bảo tính lịch sử.

---

## ✅ Giải pháp: Builder Pattern

Tách biệt logic xây dựng đối tượng `Order` phức tạp ra khỏi service chính. `OrderBuilder` hỗ trợ xây dựng đơn hàng theo phong cách Fluent API.

```java
public class OrderBuilder {
    private Customer customer;
    private String shippingAddressLine; // Lưu dạng String Snapshot
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private OrderStatus status = OrderStatus.PENDING;
    private List<OrderItem> items = new ArrayList<>();

    public static OrderBuilder builder() { return new OrderBuilder(); }

    public OrderBuilder forCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public OrderBuilder shipTo(String addressLine) {
        this.shippingAddressLine = addressLine;
        return this;
    }

    public OrderBuilder withDiscount(BigDecimal discount) {
        this.discountAmount = discount;
        return this;
    }

    public OrderBuilder withItems(List<OrderItem> items) {
        this.items = items;
        return this;
    }

    public Order build() {
        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddressLine(shippingAddressLine);
        order.setDiscountAmount(discountAmount);
        order.setStatus(status);
        order.setCreatedAt(LocalDateTime.now());
        
        for (OrderItem item : items) {
            item.setOrder(order);
        }
        order.setItems(items);
        return order;
    }
}
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| Khó đọc với 6+ tham số | Rõ nghĩa với Fluent API |
| Logic build lẫn lộn nghiệp vụ | Tách biệt hoàn toàn khâu tạo đối tượng |
| Địa chỉ phụ thuộc thực thể | Lưu chuỗi Snapshot an toàn cho dữ liệu cũ |
