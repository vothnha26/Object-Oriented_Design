# Bài toán 3: Tạo đơn hàng bằng Builder Pattern

## Pattern áp dụng

- `Builder`

---

## 1. Mục tiêu của bài toán

Quá trình checkout cần dựng một đối tượng `Order` khá phức tạp:

- có `Customer`
- có địa chỉ giao hàng
- có danh sách `OrderItem`
- mỗi `OrderItem` có thể có `OrderedTopping`
- cần gắn ngược `item -> order`
- có các field mặc định như `status`, `createdAt`, `discountAmount`

Nếu việc tạo `Order` được viết trực tiếp bằng `new Order()` và một loạt `set...()` ở nhiều chỗ khác nhau, code sẽ khó đọc, khó mở rộng và dễ thiếu bước khởi tạo quan trọng.

Mục tiêu của refactor là:

- tách logic dựng `Order` ra khỏi service nghiệp vụ
- chuẩn hóa cách tạo `Order`
- giảm việc khởi tạo thủ công ở nhiều nơi
- giúp `OrderFactory` và `CheckoutService` gọn vai trò hơn

---

## 2. Trước khi áp dụng Builder Pattern

### 2.1 Các file chính trước khi refactor

#### `src/main/java/com/alotra/service/order/OrderFactory.java`

Vai trò trước đây:

- chuyển `CartItemDTO` thành `OrderItem`
- tạo luôn đối tượng `Order`
- tự set:
  - `customer`
  - `shippingAddressLine`
  - `items`
  - back-reference `orderItem.setOrder(order)`

Vấn đề:

- file này vừa làm nhiệm vụ `factory` vừa làm nhiệm vụ `builder`
- việc khởi tạo `Order` bị trộn với việc convert item/topping
- nếu thêm field mới của `Order`, rất dễ phải sửa nhiều dòng trong method này

#### `src/main/java/com/alotra/service/order/CheckoutService.java`

Vai trò trước đây:

- trong method `createOrder(...)` cũ, file này cũng tự:
  - `new Order()`
  - set customer
  - set shipping address
  - set items
  - tạo payment

Vấn đề:

- tồn tại thêm một nơi khác cũng tự tạo `Order`
- hệ thống có nhiều cách dựng `Order` song song
- không đồng nhất

#### `src/main/java/com/alotra/service/order/CheckoutFacade.java`

Vai trò:

- điều phối luồng checkout:
  - validate stock
  - tạo order
  - tính giá
  - xử lý payment
  - lưu order

Vấn đề trước khi có builder:

- facade không trực tiếp sai, nhưng phụ thuộc vào một `OrderFactory` đang làm quá nhiều việc

---

## 3. Các vấn đề của thiết kế cũ

### 3.1 Logic tạo Order bị rải ở nhiều nơi

Ít nhất có hai nơi trực tiếp dựng `Order`:

- `OrderFactory`
- `CheckoutService`

### 3.2 Khó mở rộng

Nếu `Order` thêm field mới như:

- `promotion`
- `employee`
- `discountAmount`
- `status`
- `createdAt`

thì rất dễ quên set ở một trong các chỗ tạo đối tượng.

### 3.3 Vi phạm SRP

`OrderFactory` lẽ ra nên tập trung vào chuyển đổi dữ liệu đầu vào sang entity, nhưng lại kiêm cả trách nhiệm lắp ráp hoàn chỉnh `Order`.

### 3.4 Khó test riêng phần build

Không có một class riêng chuyên tạo `Order`, nên muốn test logic build phải đi vòng qua service hoặc factory.

---

## 4. Sau khi áp dụng Builder Pattern

### 4.1 Các file sau khi refactor và ý nghĩa của từng file

#### `src/main/java/com/alotra/service/order/OrderBuilder.java`

Đây là file trung tâm của Builder Pattern.

Vai trò:

- cung cấp fluent API để dựng `Order`
- gom tất cả dữ liệu cần thiết trước khi build
- thiết lập giá trị mặc định hợp lệ
- đảm bảo back-reference `OrderItem -> Order`

Các method chính:

- `builder()`
- `forCustomer(...)`
- `handledBy(...)`
- `withPromotion(...)`
- `shipTo(...)`
- `withDiscount(...)`
- `createdAt(...)`
- `withStatus(...)`
- `withItems(...)`
- `build()`

Ý nghĩa:

- thay vì tạo `Order` bằng nhiều câu `set...()`, giờ có một API rõ nghĩa
- khi nhìn chain builder là hiểu ngay ý định tạo đơn hàng

#### `src/main/java/com/alotra/service/order/OrderFactory.java`

Vai trò sau refactor:

- chỉ tập trung vào việc chuyển `CartItemDTO` thành `OrderItem`
- dựng topping cho từng item
- gán note cho item
- cuối cùng gọi `OrderBuilder` để lắp ráp `Order`

Giải quyết:

- `OrderFactory` không còn tự gánh phần khởi tạo `Order` thủ công
- trách nhiệm file rõ hơn:
  - convert dữ liệu đầu vào
  - gọi builder để tạo entity hoàn chỉnh

#### `src/main/java/com/alotra/service/order/CheckoutService.java`

Vai trò sau refactor:

- chỉ còn tập trung vào `saveOrder(...)`
- không còn giữ method `createOrder(...)` kiểu cũ nữa

Giải quyết:

- luồng tạo `Order` không còn bị chia thành nhiều nhánh
- `CheckoutService` quay về đúng vai trò lưu dữ liệu thay vì kiêm luôn logic build

#### `src/main/java/com/alotra/service/order/CheckoutFacade.java`

Vai trò:

- vẫn là nơi điều phối checkout

Ý nghĩa sau refactor:

- facade không cần biết chi tiết cách dựng `Order`
- chỉ gọi `orderFactory.createOrder(...)`

Như vậy:

- facade điều phối
- factory convert dữ liệu
- builder lắp ráp object

Phân vai rõ hơn trước.

#### `src/test/java/com/alotra/service/order/OrderBuilderTest.java`

Vai trò:

- test riêng cho builder

Giải quyết:

- chứng minh builder:
  - gán đúng customer
  - gán đúng địa chỉ giao hàng
  - gán mặc định `PENDING`
  - tự gắn `item.setOrder(order)`

Đây là lợi ích rất rõ của Builder Pattern: có thể test phần build tách biệt khỏi flow checkout.

---

## 5. Luồng tạo đơn sau khi áp dụng Builder Pattern

### 5.1 Trong flow checkout chính

1. `CheckoutFacade` nhận request checkout
2. facade gọi `OrderFactory.createOrder(...)`
3. `OrderFactory`:
   - convert `CartItemDTO` -> `OrderItem`
   - gắn topping
   - gắn note
4. `OrderFactory` gọi:

```java
OrderBuilder.builder()
    .forCustomer(customer)
    .shipTo(addressLine)
    .withItems(orderItems)
    .build();
```

5. `OrderBuilder.build()` tạo `Order` hoàn chỉnh
6. facade tiếp tục gọi:
   - `PriceService`
   - `PaymentService`
   - `CheckoutService.saveOrder(...)`

---

## 6. Builder Pattern đã giải quyết vấn đề gì

### 6.1 Chuẩn hóa cách tạo Order

Tất cả dữ liệu quan trọng để dựng `Order` đi qua một builder thống nhất.

### 6.2 Giảm số nơi phải `new Order()`

Thay vì nhiều nơi dựng đối tượng thủ công, giờ phần này tập trung vào `OrderBuilder`.

### 6.3 Dễ mở rộng hơn

Nếu cần thêm field mới vào `Order`, ví dụ:

- `promotion`
- `employee`
- `discountAmount`
- `createdAt`

ta có thể bổ sung method builder tương ứng mà không làm flow checkout rối thêm.

### 6.4 Tăng tính dễ đọc

Code kiểu:

```java
OrderBuilder.builder()
    .forCustomer(customer)
    .shipTo(addressLine)
    .withItems(orderItems)
    .build();
```

dễ hiểu hơn nhiều so với:

```java
Order order = new Order();
order.setCustomer(...);
order.setShippingAddressLine(...);
order.setItems(...);
...
```

### 6.5 Dễ test độc lập

Builder có test riêng mà không cần chạy toàn bộ flow checkout.

---

## 7. So sánh trước và sau

### Trước khi áp dụng

- `OrderFactory` vừa convert dữ liệu, vừa dựng `Order`
- `CheckoutService` cũng tự dựng `Order`
- nhiều chỗ `new Order()`
- khó đảm bảo mọi field được set nhất quán

### Sau khi áp dụng

- `OrderBuilder` chuyên trách việc dựng `Order`
- `OrderFactory` chỉ lo chuyển `CartItemDTO -> OrderItem`
- `CheckoutService` không còn tự tạo `Order`
- việc khởi tạo `Order` rõ nghĩa và nhất quán hơn

---

## 8. Kết luận

Sau khi áp dụng `Builder Pattern`, phần tạo đơn hàng của hệ thống đã rõ ràng hơn:

- trách nhiệm giữa các file được tách tốt hơn
- logic build `Order` không còn rải rác
- code checkout dễ đọc hơn
- dễ thêm thuộc tính mới cho `Order`
- có thể test builder độc lập

Builder không thay thế `OrderFactory` hay `CheckoutFacade`, mà làm cho hai thành phần này gọn và đúng vai trò hơn:

- `CheckoutFacade` điều phối nghiệp vụ
- `OrderFactory` chuyển đổi dữ liệu đầu vào
- `OrderBuilder` xây dựng đối tượng `Order`
