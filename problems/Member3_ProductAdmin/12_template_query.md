# 🔴 Bài toán 12: Query pipeline lặp lại + Filter cứng nhắc

## Patterns: **Template Method** + **Strategy**
## SOLID vi phạm: **DRY**, **OCP**, **SRP**

---

## 📌 Vấn đề hiện tại

Hệ thống quản lý đơn hàng có nhiều nhu cầu truy vấn khác nhau: xem đơn hàng mới, xem đơn hàng đang xử lý, xem lịch sử đơn hàng của khách. Hiện tại, các method truy vấn này có cấu trúc pipeline giống hệt nhau: `findAll()` → filter → keyword search → sort → limit → map to DTO. Code bị **copy-paste** nghiêm trọng:

### Method Ví dụ: `getAvailableOrders()`
```java
public List<OrderDto> getAvailableOrders(String keyword, Integer limit) {
    // Bước 1: Load TẤT CẢ đơn hàng và lọc đơn hàng chờ xử lý
    List<Order> orders = orderRepository.findAll().stream()
        .filter(o -> OrderStatus.PENDING.equals(o.getStatus()))
        .collect(Collectors.toList());

    // Bước 2: Filter keyword (COPY-PASTE lặp lại ở nhiều nơi)
    if (keyword != null && !keyword.isBlank()) {
        String kw = keyword.toLowerCase();
        orders = orders.stream()
            .filter(o -> {
                String customerName = o.getCustomer() != null ? o.getCustomer().getFullName().toLowerCase() : "";
                String address = o.getShippingAddressLine() != null ? o.getShippingAddressLine().toLowerCase() : "";
                return customerName.contains(kw) || address.contains(kw);
            }).collect(Collectors.toList());
    }

    // Bước 3: Sort (COPY-PASTE!)
    orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

    // Bước 4: Limit (COPY-PASTE!)
    if (limit != null && limit > 0 && orders.size() > limit) {
        orders = orders.subList(0, limit);
    }

    // Bước 5: Map to DTO (COPY-PASTE!)
    return orders.stream().map(this::toDto).collect(Collectors.toList());
}
```

### ⚠️ Vấn đề cụ thể
1. **Vi phạm DRY**: Pipeline filter→sort→limit→map lặp lại ở nhiều Service khác nhau.
2. **Vi phạm OCP**: Thêm một loại truy vấn mới (VD: đơn hàng cần hoàn tiền) → lại copy-paste toàn bộ pipeline.
3. **Vi phạm SRP**: Service vừa chứa logic nghiệp vụ, vừa lo việc lọc, sắp xếp, phân trang.
4. **Performance tệ**: `findAll()` load **TOÀN BỘ** dữ liệu từ DB rồi filter trong Java.

---

## ✅ Giải pháp: Template Method + Strategy

### Strategy Pattern: Filter linh hoạt

```java
// ===== Strategy Interface: Điều kiện lọc =====
@FunctionalInterface
public interface OrderFilterStrategy {
    boolean matches(Order order);
}

// ===== Concrete Strategies =====
public class StatusOrderFilter implements OrderFilterStrategy {
    private final OrderStatus status;
    public StatusOrderFilter(OrderStatus status) { this.status = status; }

    @Override
    public boolean matches(Order order) {
        return status.equals(order.getStatus());
    }
}

// ===== Keyword Filter (composable Strategy) =====
public class KeywordFilter implements OrderFilterStrategy {
    private final String keyword;
    public KeywordFilter(String keyword) { this.keyword = keyword.toLowerCase(); }

    @Override
    public boolean matches(Order order) {
        String customerName = order.getCustomer() != null ? order.getCustomer().getFullName().toLowerCase() : "";
        String address = order.getShippingAddressLine() != null ? order.getShippingAddressLine().toLowerCase() : "";
        return customerName.contains(keyword) || address.contains(keyword);
    }
}
```

### Template Method: Pipeline cố định

```java
// ===== Abstract Query Pipeline =====
public abstract class AbstractOrderQuery {
    private final OrderRepository repository;

    protected AbstractOrderQuery(OrderRepository repository) {
        this.repository = repository;
    }

    // === TEMPLATE METHOD: các bước cố định ===
    public final List<OrderDto> execute(String keyword, Integer limit) {
        List<Order> orders = fetchOrders(); // Bước 1: Fetch

        orders = orders.stream()
            .filter(o -> getFilter().matches(o)) // Bước 2: Filter chính
            .collect(Collectors.toList());

        if (keyword != null && !keyword.isBlank()) { // Bước 3: Keyword filter (hook)
            OrderFilterStrategy kwFilter = new KeywordFilter(keyword);
            orders = orders.stream().filter(kwFilter::matches).collect(Collectors.toList());
        }

        orders.sort(getComparator()); // Bước 4: Sort

        if (limit != null && limit > 0 && orders.size() > limit) { // Bước 5: Limit
            orders = orders.subList(0, limit);
        }

        return orders.stream().map(this::toDto).collect(Collectors.toList()); // Bước 6: Map
    }

    protected abstract OrderFilterStrategy getFilter();
    protected List<Order> fetchOrders() { return repository.findAll(); }
    protected Comparator<Order> getComparator() { 
        return (a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()); 
    }
    protected abstract OrderDto toDto(Order o);
}
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| Code lọc lặp lại 3-4 lần | Pipeline viết 1 lần duy nhất |
| Filter cứng nhắc | `OrderFilterStrategy` thay đổi linh hoạt |
| Khó tối ưu Query | `fetchOrders()` dễ dàng được override để dùng Custom Query |
