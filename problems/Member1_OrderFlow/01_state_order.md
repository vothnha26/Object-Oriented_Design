# 🔴 Bài toán 1: Quản lý trạng thái đơn hàng

## Patterns: **State** + **Command**
## SOLID vi phạm: **OCP** (Open/Closed), **SRP** (Single Responsibility)

---

## 📌 Vấn đề hiện tại

Trạng thái đơn hàng (`Order.status`) được quản lý bằng **chuỗi String** và logic chuyển trạng thái nằm **rải rác** trong nhiều file, sử dụng `if/switch` lặp đi lặp lại.

### Code có vấn đề

**`VendorOrderService.java`** — dùng `switch` để tính trạng thái tiếp theo:
```java
public OrderStatus nextStatus(OrderStatus current) {
    if (current == null) return OrderStatus.PENDING;
    return switch (current) {
        case OrderStatus.PENDING -> OrderStatus.PREPARING;
        case OrderStatus.PREPARING -> OrderStatus.DELIVERING;
        case OrderStatus.DELIVERING -> OrderStatus.DELIVERED;
        default -> current;
    };
}

public boolean canCancel(OrderStatus current) {
    return current.equals(OrderStatus.PENDING) || current.equals(OrderStatus.PREPARING);
}
```

**`ShipperOrderService.java`** — **copy-paste y hệt** cùng logic:
```java
private String getNextStatus(OrderStatus currentStatus) {
    switch (currentStatus) {
        case OrderStatus.PENDING: return OrderStatus.PREPARING;
        case OrderStatus.PREPARING: return OrderStatus.DELIVERING;
        case OrderStatus.DELIVERING: return OrderStatus.DELIVERED;
        default: return null;
    }
}

private boolean canCancel(OrderStatus status) {
    return OrderStatus.PENDING.equals(status) || OrderStatus.PREPARING.equals(status);
}
```

**`ReviewService.java`** — kiểm tra trạng thái bằng hardcode string:
```java
public boolean isOrderEligibleForReview(OrderStatus orderStatus, PaymentStatus paymentStatus) {
    return OrderStatus.DELIVERED.equals(orderStatus) && PaymentStatus.PAID.equals(paymentStatus);
}
```

### ❌ Vấn đề cụ thể

1. **Vi phạm OCP**: Thêm trạng thái mới (VD: "DangDoiHang") → phải sửa **tất cả** switch/if trong 3+ file
2. **Vi phạm SRP**: `VendorOrderService` kiêm luôn logic chuyển trạng thái, kiểm tra hủy, validate
3. **Vi phạm DRY**: Logic `nextStatus()` và `canCancel()` bị **duplicate** giữa `VendorOrderService` và `ShipperOrderService`
4. **Dễ lỗi**: Dùng magic strings (OrderStatus.PENDING, OrderStatus.DELIVERING) → typo sẽ không bị phát hiện compile-time
5. **Thiếu validation**: Không kiểm tra chuyển trạng thái bất hợp lệ (VD: DaGiao → ChoXuLy)

---

## ✅ Giải pháp: State Pattern + Command Pattern

### State Pattern
Mỗi trạng thái đơn hàng là một **class riêng** implement interface `OrderState`. Đơn hàng giữ reference tới state hiện tại → delegate hành vi.

### Command Pattern
Mỗi thao tác chuyển trạng thái (advance, cancel, markDelivered) là một **Command** có thể execute + undo.

### Thiết kế mới

```
                    «interface»
                    OrderState
              ┌─────────────────────┐
              │ + advance(context)  │
              │ + cancel(context)   │
              │ + canCancel(): bool │
              │ + canReview(): bool │
              │ + getStatusName()   │
              └────────┬────────────┘
                       │ implements
       ┌───────────────┼────────────────┐──────────────┐──────────────┐
       ▼               ▼                ▼              ▼              ▼
   PendingState    PreparingState   DeliveringState   DeliveredState   CancelledState
  ┌──────────┐  ┌──────────────┐  ┌────────────┐  ┌──────────┐  ┌─────────┐
  │advance →│  │advance →    │  │advance →  │  │canReview │  │(final) │
  │Preparing │  │Delivering   │  │Delivered  │  │  = true  │  │canCancel│
  │canCancel │  │canCancel    │  │canCancel  │  │          │  │ = false│
  │  = true  │  │  = true     │  │  = false  │  │          │  │        │
  └──────────┘  └──────────────┘  └────────────┘  └──────────┘  └─────────┘
```

### Code mẫu sau refactor

```java
// ===== Interface =====
public interface OrderState {
    void advance(OrderContext context);    // Chuyển trạng thái tiếp
    void cancel(OrderContext context);     // Hủy đơn
    boolean canCancel();
    boolean canReview();
    String getStatusName();
}

// ===== Concrete State =====
public class PendingState implements OrderState {
    @Override
    public void advance(OrderContext ctx) {
        ctx.setState(new PreparingState());
    }
    @Override
    public void cancel(OrderContext ctx) {
        ctx.setState(new CancelledState());
    }
    @Override public boolean canCancel() { return true; }
    @Override public boolean canReview() { return false; }
    @Override public String getStatusName() { return OrderStatus.PENDING.name(); }
}

public class DeliveringState implements OrderState {
    @Override
    public void advance(OrderContext ctx) {
        ctx.setState(new DeliveredState());
    }
    @Override
    public void cancel(OrderContext ctx) {
        throw new IllegalStateException("Không thể hủy đơn đang giao");
    }
    @Override public boolean canCancel() { return false; }
    @Override public boolean canReview() { return false; }
    @Override public String getStatusName() { return OrderStatus.DELIVERING.name(); }
}

// ===== Context =====
public class OrderContext {
    private OrderState state;
    private Order order;

    public OrderContext(Order order) {
        this.order = order;
        this.state = OrderStateFactory.fromString(order.getStatus());
    }
    
    public void advance() { state.advance(this); }
    public void cancel() { state.cancel(this); }
    public boolean canCancel() { return state.canCancel(); }

    public OrderState getState() { return state; }
    public void setState(OrderState state) { this.state = state; }
}

// ===== Factory tạo State từ String (kết hợp Factory) =====
public class OrderStateFactory {
    public static OrderState fromString(OrderStatus status) {
        return switch (status) {
            case OrderStatus.PENDING -> new PendingState();
            case OrderStatus.PREPARING -> new PreparingState();
            case OrderStatus.DELIVERING -> new DeliveringState();
            case OrderStatus.DELIVERED -> new DeliveredState();
            case OrderStatus.CANCELLED -> new CancelledState();
            default -> throw new IllegalArgumentException("Unknown status: " + status);
        };
    }
}
```

### Kết hợp Command Pattern cho undo:

```java
public interface OrderCommand {
    void execute();
    void undo();
}

public class AdvanceOrderCommand implements OrderCommand {
    private final OrderContext context;
    private final OrderState previousState;

    public AdvanceOrderCommand(OrderContext context) {
        this.context = context;
        this.previousState = context.getState(); // lưu trạng thái cũ
    }

    @Override
    public void execute() { context.advance(); }

    @Override
    public void undo() { context.setState(previousState); }
}
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| 3+ file chứa logic switch | 1 interface + 5 state classes |
| Thêm trạng thái → sửa nhiều file | Thêm trạng thái → tạo 1 class mới |
| Magic strings → runtime error | Compile-time safety |
| Không undo được | Command cho phép undo |
