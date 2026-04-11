# 🔴 Bài toán 3: Tạo đơn hàng quá phức tạp

## Pattern: **Builder**
## SOLID vi phạm: **SRP**

---

## 📌 Vấn đề hiện tại

Method `processCheckout()` trong `CheckoutService.java` nhận **8 tham số** và chứa logic **90+ dòng** liên tục:

```java
public Order processCheckout(
    Customer kh,
    List<Integer> itemIds,
    PaymentMethod paymentMethod,
    String note,
    String receivingMethod,  // "Ship" | "Pickup"
    String shipName,
    String shipPhone,
    String shipAddress
) {
    // 90+ dòng logic: validate → build Order → build CTDonHang → build CTDonHangTopping → cleanup cart
}
```

### ⚠️ Vấn đề cụ thể
1. **Quá nhiều tham số**: 8 params → khó đọc, dễ nhầm thứ tự.
2. **Logic xây dựng Order phức tạp**: Set ~15 field, tính toán vận chuyển, xử lý Ship vs Pickup, note composite.
3. **Vi phạm SRP**: Method vừa validate, vừa build order, vừa build lines, vừa tính topping, vừa cleanup giỏ hàng.
4. **Khó test**: Không thể test riêng phần tạo Order.

---

## ✅ Giải pháp: Builder Pattern

Tách biệt logic xây dựng đối tượng `Order` ra khỏi Business Service. `OrderBuilder` được tích hợp vào `OrderFactory` để hỗ trợ quá trình chuyển đổi DTO sang Entity một cách minh bạch.

```java
public class OrderBuilder {
    private Customer customer;
    private Employee employee;
    private Promotion promotion;
    private String shippingAddressLine;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private OrderStatus status = OrderStatus.PENDING;
    private List<OrderItem> items = new ArrayList<>();

    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    public OrderBuilder forCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public OrderBuilder shipTo(String addressLine) {
        this.shippingAddressLine = addressLine;
        return this;
    }

    public OrderBuilder withDiscount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
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

### Sử dụng (Trong OrderFactory):

```java
public Order createOrder(Customer customer, Address address, List<CartItemDTO> cartItems) {
    List<OrderItem> orderItems = // ... convert cartItems to OrderItems
    
    return OrderBuilder.builder()
        .forCustomer(customer)
        .shipTo(address != null ? address.getAddressLine() : null)
        .withItems(orderItems)
        .build();
}
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| 8 params → nhầm thứ tự | Fluent API → rõ nghĩa |
| Validation + Build lẫn lộn | Tách riêng logic xây dựng vào Builder |
| Thêm field → sửa method signature | Thêm field → thêm method builder |
| Khó test | Builder có thể test độc lập |
