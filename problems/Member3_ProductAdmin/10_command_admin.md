# 🔴 Bài toán 10: Admin thao tác không undo được

## Pattern: **Command**
## SOLID vi phạm: **SRP**, **OCP**

---

## 📌 Vấn đề hiện tại

Admin thực hiện nhiều thao tác quan trọng nhưng **không thể undo**, và logic xử lý nằm **trực tiếp** trong controller/service:

### Code có vấn đề

**Soft delete sản phẩm** trong `AdminProductController`:
```java
// Xóa mềm — không undo được nếu nhầm
product.setDeletedAt(LocalDateTime.now());
productRepo.save(product);
```

**Soft delete nhân viên** trong `EmployeeService`:
```java
public void softDeleteToTrash(Integer id) {
    employeeRepository.findById(id).ifPresent(employee -> {
        employee.setStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
    });
}
```

**Cập nhật trạng thái đơn hàng** trong `VendorOrderService`:
```java
public void updateStatus(Integer id, OrderStatus newStatus) {
    jdbc.update("UPDATE orders SET status = ? WHERE id = ?", newStatus.name(), id);
    // Không lưu trạng thái cũ → không undo được
}
```

### ❌ Vấn đề
1. **Không undo**: Admin xóa nhầm sản phẩm → phải vào DB sửa tay
2. **Không lịch sử**: Không biết ai làm gì, lúc nào
3. **Vi phạm SRP**: Controller xử lý trực tiếp logic delete/update
4. **Vi phạm OCP**: Thêm thao tác mới → sửa controller

---

## ✅ Giải pháp: Command Pattern

```java
// ===== Command Interface =====
public interface AdminCommand {
    void execute();
    void undo();
    String getDescription();  // mô tả thao tác cho audit log
}

// ===== Concrete Commands =====
public class SoftDeleteProductCommand implements AdminCommand {
    private final ProductRepository repo;
    private final Integer productId;
    private ProductStatus previousStatus; // lưu trạng thái cũ cho undo

    public SoftDeleteProductCommand(ProductRepository repo, Integer productId) {
        this.repo = repo;
        this.productId = productId;
    }

    @Override
    public void execute() {
        Product p = repo.findById(productId).orElseThrow();
        this.previousStatus = p.getStatus();  // lưu trước khi sửa
        p.setStatus(ProductStatus.INACTIVE);
        repo.save(p);
    }

    @Override
    public void undo() {
        Product p = repo.findById(productId).orElseThrow();
        p.setStatus(previousStatus);  // khôi phục
        repo.save(p);
    }

    @Override
    public String getDescription() {
        return "Xóa mềm sản phẩm #" + productId;
    }
}

public class UpdateOrderStatusCommand implements AdminCommand {
    private final OrderRepository repo;
    private final Integer orderId;
    private final OrderStatus newStatus;
    private OrderStatus previousStatus;

    @Override
    public void execute() {
        Order order = repo.findById(orderId).orElseThrow();
        this.previousStatus = order.getStatus();
        order.setStatus(newStatus);
        repo.save(order);
    }

    @Override
    public void undo() {
        Order order = repo.findById(orderId).orElseThrow();
        order.setStatus(previousStatus);
        repo.save(order);
    }

    @Override
    public String getDescription() {
        return "Đơn #" + orderId + ": " + previousStatus + " → " + newStatus;
    }
}

// ===== Invoker (quản lý lịch sử + undo) =====
@Service
public class AdminCommandInvoker {
    private final Deque<AdminCommand> history = new ArrayDeque<>();
    private static final int MAX_HISTORY = 50;

    public void execute(AdminCommand command) {
        command.execute();
        history.push(command);
        if (history.size() > MAX_HISTORY) {
            history.removeLast(); // giới hạn memory
        }
        // Audit log
        log.info("[ADMIN ACTION] {}", command.getDescription());
    }

    public boolean undo() {
        if (history.isEmpty()) return false;
        AdminCommand last = history.pop();
        last.undo();
        log.info("[ADMIN UNDO] {}", last.getDescription());
        return true;
    }
}
```

### Sử dụng trong Controller:

```java
@PostMapping("/admin/products/{id}/delete")
public String deleteProduct(@PathVariable Integer id) {
    AdminCommand cmd = new SoftDeleteProductCommand(productRepo, id);
    commandInvoker.execute(cmd);  // execute + log + push to history
    return "redirect:/admin/products";
}

@PostMapping("/admin/undo")
public String undoLastAction() {
    boolean success = commandInvoker.undo();
    return "redirect:/admin/dashboard?undo=" + success;
}
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| Xóa nhầm → vào DB sửa | `commandInvoker.undo()` |
| Không biết ai làm gì | Audit log tự động |
| Controller xử lý trực tiếp | Controller chỉ tạo Command + invoke |
| Thêm thao tác → sửa controller | Thêm `XxxCommand implements AdminCommand` |

---

## 📋 Tổng kết: Mapping Patterns ↔ Bài toán

| # | Pattern | Bài toán & Module liên quan |
|---|---------|----------------------------|
| 1 | **State** | [01] Quản lý trạng thái đơn hàng (OrderFlow) |
| 2 | **Strategy** | [02] Thanh toán & Giảm giá (Checkout) |
| 3 | **Builder** | [03] Tạo đơn hàng phức tạp (OrderFlow) |
| 4 | **Observer** | [04] Thông báo khi đơn hàng thay đổi (OrderFlow) |
| 5 | **Decorator** | [05] Tính giá sản phẩm: base → promotion → topping (ProductAdmin) |
| 6 | **Facade** | [06] Tách CheckoutService khổng lồ (Checkout) |
| 7 | **Template Method** | [07] Quy trình xử lý mật khẩu (UserSecurity) |
| 8 | **Factory/Singleton** | [08] Tạo User & Singleton Instance (UserSecurity) |
| 9 | **Proxy** | [09] Bảo mật & Logging (UserSecurity) |
| 10 | **Command** | [10] Thao tác hoàn tác (Undo) của Admin (ProductAdmin) |
| 11 | **Adapter** | [11] Lưu trữ ảnh đa nền tảng (Checkout) |
| 12 | **Template Method** | [12] Pipeline truy vấn dữ liệu (ProductAdmin) |
