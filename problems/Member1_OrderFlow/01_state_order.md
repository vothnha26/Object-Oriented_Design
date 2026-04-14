# Bài toán 1: Quản lý trạng thái đơn hàng bằng State Pattern

## Pattern áp dụng

- `State`
- Phần mở rộng đã tích hợp thêm: `Command` để hỗ trợ undo đổi trạng thái

---

## 1. Mục tiêu của bài toán

Đơn hàng trong hệ thống có nhiều trạng thái như:

- `PENDING`
- `PREPARING`
- `DELIVERING`
- `DELIVERED`
- `CANCELLED`

Nếu logic chuyển trạng thái được viết bằng `if` hoặc `switch` rải rác trong nhiều file, hệ thống sẽ khó mở rộng, dễ sai rule nghiệp vụ và khó bảo trì.
Vì vậy, mục tiêu của refactor là:

- gom logic trạng thái về một nơi
- để mỗi trạng thái tự quyết định hành vi của mình
- giảm việc sửa nhiều file khi thêm trạng thái mới
- hỗ trợ undo khi đổi trạng thái

---

## 2. Trước khi áp dụng State Pattern

### 2.1 Các file chính trước khi refactor

#### `src/main/java/com/alotra/service/order/VendorOrderService.java`

Vai trò trước đây:

- tính trạng thái kế tiếp
- kiểm tra có được hủy hay không
- cập nhật trạng thái đơn

Vấn đề:

- `VendorOrderService` vừa làm service nghiệp vụ, vừa giữ toàn bộ luật chuyển trạng thái
- logic `nextStatus()` và `canCancel()` bị hardcode
- khi thêm trạng thái mới phải sửa trực tiếp ở đây

#### `src/main/java/com/alotra/controller/vendor/VendorController.java`

Vai trò trước đây:

- lấy trạng thái hiện tại từ `Order`
- gọi service để tính trạng thái mới
- quyết định khi nào được chuyển bước hoặc hủy

Vấn đề:

- controller phải biết quá nhiều về luật trạng thái
- flow nghiệp vụ bị trải ra giữa controller và service

#### `src/main/java/com/alotra/controller/account/PaymentController.java`

Vai trò trước đây:

- tự hủy đơn khi thanh toán quá hạn
- tự hủy đơn khi khách bấm hủy

Vấn đề:

- controller set thẳng `order.setStatus(OrderStatus.CANCELLED)`
- bỏ qua rule trạng thái chung của hệ thống
- nếu sau này có state mới hoặc luật hủy thay đổi thì phải sửa thêm ở đây

#### `src/main/java/com/alotra/service/interaction/ReviewService.java`

Vai trò trước đây:

- kiểm tra đơn hàng có đủ điều kiện review hay không

Vấn đề:

- điều kiện review bị hardcode theo kiểu `orderStatus == DELIVERED`
- logic phụ thuộc vào trạng thái nhưng không nằm trong mô hình trạng thái

---

## 3. Các vấn đề của thiết kế cũ

### 3.1 Vi phạm OCP

Khi thêm trạng thái mới, ví dụ `RETURN_REQUESTED`, phải sửa ở nhiều nơi:

- `VendorOrderService`
- `PaymentController`
- `ReviewService`
- có thể thêm cả controller hoặc service khác

### 3.2 Vi phạm SRP

`VendorOrderService` không chỉ xử lý đơn hàng mà còn phải nắm toàn bộ luật trạng thái.

### 3.3 Dễ lệch rule giữa các nơi

Ví dụ:

- một nơi cho phép hủy
- nơi khác lại cấm hủy
- một nơi hiểu `DELIVERED` mới được review
- nơi khác có thể xử lý khác

### 3.4 Khó kiểm soát chuyển trạng thái sai

Nếu không gom rule về một chỗ, các chuyển trạng thái bất hợp lệ như:

- `DELIVERED -> PREPARING`
- `CANCELLED -> DELIVERING`

sẽ khó bị chặn đồng nhất.

---

## 4. Sau khi áp dụng State Pattern

### 4.1 Các file mới và vai trò của từng file

#### `src/main/java/com/alotra/entity/state/OrderState.java`

Ý nghĩa:

- interface chung cho mọi trạng thái đơn hàng
- định nghĩa các hành vi mà mọi state phải có:
  - `advance(...)`
  - `cancel(...)`
  - `canCancel()`
  - `canReview()`
  - `getStatus()`

Giá trị:

- chuẩn hóa hành vi giữa các state
- khi nhìn vào interface là biết hệ thống đang hỗ trợ những thao tác trạng thái nào

#### `src/main/java/com/alotra/entity/state/PendingState.java`

Ý nghĩa:

- mô tả luật của trạng thái `PENDING`

Giải quyết:

- state này tự quyết định:
  - được `advance` sang `PREPARING`
  - được `cancel`
  - chưa được `review`

#### `src/main/java/com/alotra/entity/state/PreparingState.java`

Ý nghĩa:

- mô tả luật của trạng thái `PREPARING`

Giải quyết:

- chuyển sang `DELIVERING`
- vẫn cho phép hủy

#### `src/main/java/com/alotra/entity/state/DeliveringState.java`

Ý nghĩa:

- mô tả luật của trạng thái `DELIVERING`

Giải quyết:

- chuyển sang `DELIVERED`
- không cho phép hủy

#### `src/main/java/com/alotra/entity/state/DeliveredState.java`

Ý nghĩa:

- mô tả trạng thái hoàn tất

Giải quyết:

- không cho `advance`
- không cho hủy
- cho phép review

#### `src/main/java/com/alotra/entity/state/CancelledState.java`

Ý nghĩa:

- mô tả trạng thái kết thúc thất bại

Giải quyết:

- không cho `advance`
- không cho `cancel` lần nữa
- không cho review

#### `src/main/java/com/alotra/entity/state/OrderStateFactory.java`

Ý nghĩa:

- ánh xạ `OrderStatus` enum sang class state tương ứng

Giải quyết:

- thay vì switch ở nhiều nơi, giờ chỉ còn một điểm tạo state
- khi thêm state mới, chỗ cần sửa tập trung hơn

#### `src/main/java/com/alotra/entity/state/OrderContext.java`

Ý nghĩa:

- là context của State Pattern
- giữ:
  - `Order` hiện tại
  - `OrderState` hiện tại

Giải quyết:

- các controller/service không cần tự nghĩ xem trạng thái kế tiếp là gì
- chỉ cần gọi:
  - `context.advance()`
  - `context.cancel()`
  - `context.canCancel()`
  - `context.canReview()`

#### `src/main/java/com/alotra/service/order/VendorOrderService.java`

Vai trò sau refactor:

- không còn tự tính trạng thái bằng `if/switch`
- chỉ gọi command đổi trạng thái hợp lệ là `advance` hoặc `cancel`
- kiểm tra hủy thông qua `OrderContext`

Giải quyết:

- service trở thành nơi điều phối nghiệp vụ
- luật trạng thái được đẩy về state classes
- không còn API `updateStatus(...)` để set trạng thái trực tiếp từ bên ngoài

#### `src/main/java/com/alotra/controller/vendor/VendorController.java`

Vai trò sau refactor:

- chỉ nhận request và gọi service
- không còn tự tính `nextStatus`

Giải quyết:

- controller mỏng hơn
- ít phụ thuộc vào chi tiết luật trạng thái

#### `src/main/java/com/alotra/controller/account/PaymentController.java`

Vai trò sau refactor:

- khi hủy đơn do quá hạn hoặc do khách thao tác, controller dùng `OrderContext`

Giải quyết:

- mọi chỗ hủy đơn dùng cùng một rule
- không còn set thẳng `CANCELLED` một cách rời rạc

#### `src/main/java/com/alotra/service/interaction/ReviewService.java`

Vai trò sau refactor:

- kiểm tra quyền review thông qua state

Giải quyết:

- điều kiện review không còn hardcode cứng theo enum ở một chỗ riêng
- state `DeliveredState` trở thành nơi nói rõ rằng đơn đã giao thì được review

---

## 5. Luồng hoạt động sau khi refactor

### 5.1 Chuyển bước đơn hàng

1. `VendorController` nhận request chuyển bước
2. gọi `VendorOrderService.advance(orderId)`
3. service tạo command đổi trạng thái
4. command lấy `Order`
5. command tạo `OrderContext`
6. `OrderContext` gọi `state.advance(this)`
7. concrete state quyết định state tiếp theo
8. `Order` được cập nhật status và lưu lại

### 5.2 Hủy đơn hàng

1. controller gọi `canCancel(order)` hoặc `cancel(orderId)`
2. `OrderContext` hỏi state hiện tại
3. nếu state cho phép thì mới chuyển sang `CancelledState`

### 5.3 Kiểm tra review

1. `ReviewService` nhận `orderStatus`
2. `OrderStateFactory` tạo ra state tương ứng
3. gọi `canReview()`
4. hệ thống biết đơn đó có được đánh giá hay không

---

## 6. State Pattern đã giải quyết vấn đề gì

### 6.1 Gom luật trạng thái về đúng chỗ

Mỗi trạng thái tự mô tả:

- đi tiếp thế nào
- có hủy được không
- có review được không

### 6.2 Giảm phụ thuộc vào `if/switch` rải rác

Controller và service không còn phải hardcode trạng thái kế tiếp.

### 6.3 Dễ mở rộng hơn

Khi thêm state mới, cách làm sẽ là:

1. thêm giá trị mới vào `OrderStatus`
2. tạo class state mới
3. đăng ký trong `OrderStateFactory`
4. chỉnh các state liên quan nếu có transition mới

So với thiết kế cũ, số điểm phải sửa đã ít và rõ ràng hơn nhiều.

### 6.4 Tăng tính nhất quán

Một rule như "đơn đang giao thì không được hủy" giờ chỉ nằm trong `DeliveringState`, thay vì bị copy ở nhiều nơi.

---

## 7. Phần mở rộng: Command để undo trạng thái

Ngoài State Pattern, hệ thống hiện tại còn tích hợp thêm Command cho đổi trạng thái.

### Các file liên quan

#### `src/main/java/com/alotra/service/command/OrderCommandInvoker.java`

- lưu lịch sử command theo session
- hỗ trợ `execute()` và `undo()`

#### `src/main/java/com/alotra/service/command/OrderStatusTransitionCommand.java`

- command thực thi chuyển trạng thái
- lưu trạng thái cũ để hoàn tác
- chỉ hỗ trợ các chuyển đổi đi qua luật state (`advance`, `cancel`)

#### `src/main/java/com/alotra/service/command/UpdateOrderStatusCommand.java`

- wrapper command theo đúng tên bài toán
- gọi vào command transition thực tế

### Ý nghĩa

- State quyết định luật chuyển trạng thái
- Command bao bọc thao tác đó để có thể undo

Đây là hai pattern bổ trợ tốt cho nhau:

- `State` lo "được chuyển như thế nào"
- `Command` lo "thực thi thao tác và hoàn tác nếu cần"

### Ghi chú về tính chặt chẽ của thiết kế

Ở phiên bản hiện tại, luồng ứng dụng không còn dùng API service/command để `setStatus(...)` trực tiếp cho đơn hàng nữa. Việc đổi trạng thái trong order flow đi qua `OrderContext` hoặc command bọc `OrderContext`, nên rule của từng state được giữ nhất quán hơn trước.

---

## 8. Kết luận

Sau khi áp dụng `State Pattern`, thiết kế quản lý trạng thái đơn hàng đã rõ ràng hơn:

- controller mỏng hơn
- service ít hardcode hơn
- luật nghiệp vụ nằm đúng trong từng state
- dễ mở rộng hơn khi thêm trạng thái mới
- giảm nguy cơ lệch rule giữa các file

Khi kết hợp thêm `Command`, hệ thống còn có thể undo thay đổi trạng thái, làm cho flow xử lý đơn hàng linh hoạt và sát với yêu cầu bài toán hơn.
