# 🔴 Bài toán 4: Thiếu cơ chế thông báo khi đơn hàng thay đổi

## Pattern: **Observer**
## SOLID vi phạm: **OCP**, **SRP**

---

## 📌 Vấn đề hiện tại

Khi trạng thái đơn hàng thay đổi, hệ thống **KHÔNG** tự động thông báo cho các bên liên quan. Hiện tại:

### Code có vấn đề

**`VendorOrderService.java`** — chỉ update DB, không thông báo ai:
```java
public void updateStatus(Integer id, OrderStatus newStatus) {
    jdbc.update("UPDATE orders SET status = ? WHERE id = ?", newStatus.name(), id);
    // Đã xong — không email, không log, không notification gì cả
}
```

**`ShipperOrderService.java`** — tương tự, chỉ set status:
```java
order.setStatus(OrderStatus.DELIVERED);
orderRepository.save(order);
// Không thông báo cho khách hàng
```

**`NotificationService.java`** — chỉ **pull-based** (khi user refresh trang), không **push-based**:
```java
public Map<String, Object> getCustomerNotifications(Integer customerId) {
    // Chỉ query DB khi user request → không real-time
}
```

### ❌ Vấn đề cụ thể
1. Khách hàng **không biết** đơn đang được pha chế hay đang giao
2. Vendor **không biết** đơn mới vừa tạo (phải F5 liên tục)
3. Nếu muốn thêm gửi email khi đơn giao xong → phải **sửa** method `updateStatus()` → **vi phạm OCP**
4. Logic thông báo nếu thêm vào method hiện tại → `ShipperOrderService` sẽ kiêm luôn gửi mail → **vi phạm SRP**

---

## ✅ Giải pháp: Observer Pattern

```java
// ===== Event =====
public class OrderStatusChangedEvent {
    private final Order order;
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;

    public OrderStatusChangedEvent(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        this.order = order;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
    // getters...
}

// ===== Observer Interface =====
public interface OrderEventListener {
    void onOrderStatusChanged(OrderStatusChangedEvent event);
}

// ===== Concrete Observers =====
@Component
public class CustomerEmailNotifier implements OrderEventListener {
    private final EmailService emailService;
    private final CustomerRepository customerRepository;

    @Override
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        Customer customer = event.getOrder().getCustomer();
        emailService.send(customer.getEmail(),
            "StarCinema - Đơn hàng #" + event.getOrder().getId(),
            "Đơn hàng của bạn đã chuyển sang trạng thái: " + event.getNewStatus());
    }
}

@Component
public class DashboardStatsUpdater implements OrderEventListener {
    @Override
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        // Invalidate cache dashboard stats
        // Broadcast WebSocket cho admin biết refresh
    }
}

@Component
public class OrderActivityLogger implements OrderEventListener {
    @Override
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Đơn #{}: {} → {}", 
            event.getOrder().getId(), event.getOldStatus(), event.getNewStatus());
    }
}

// ===== Subject (phát sự kiện) =====
@Service
public class OrderEventPublisher {
    private final List<OrderEventListener> listeners;

    public OrderEventPublisher(List<OrderEventListener> listeners) {
        this.listeners = listeners; // Spring auto-inject tất cả implementation
    }

    public void fireStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(order, oldStatus, newStatus);
        for (OrderEventListener listener : listeners) {
            listener.onOrderStatusChanged(event);
        }
    }
}
```

### Sử dụng trong service:

```java
// VendorOrderService hoặc ShipperOrderService
public void updateStatus(Integer id, OrderStatus newStatus) {
    Order order = orderRepository.findById(id).orElseThrow();
    OrderStatus oldStatus = order.getStatus();
    order.setStatus(newStatus);
    orderRepository.save(order);
    
    // 🔔 Phát sự kiện → tất cả observers tự phản ứng
    eventPublisher.fireStatusChanged(order, oldStatus, newStatus);
}
```

### Hoặc dùng Spring ApplicationEvent (built-in Observer):

```java
// Spring cung cấp sẵn cơ chế Observer
@Component
public class CustomerEmailNotifier {
    @EventListener
    public void handleOrderStatusChange(OrderStatusChangedEvent event) {
        // Gửi email...
    }
}

// Publish event
applicationEventPublisher.publishEvent(new OrderStatusChangedEvent(order, old, new));
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| Không thông báo gì | Email + log + dashboard + ... |
| Thêm thông báo SMS → sửa service | Thêm `SmsNotifier implements OrderEventListener` |
| Service biết về email/log | Service chỉ phát event, không biết ai lắng nghe |
| Không mở rộng được | Thêm observer bất kỳ lúc nào → OCP |
