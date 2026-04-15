# 🔴 Bài toán 9: Kiểm tra quyền lặp lại khắp nơi

## Pattern: **Proxy**
## SOLID vi phạm: **SRP**, **DRY**

---

## 📌 Vấn đề hiện tại

Logic kiểm tra quyền sở hữu (ownership) bị **lặp đi lặp lại** trong nhiều method, làm loãng logic nghiệp vụ chính:

**`CheckoutService.java`**:
```java
private void validateOwnership(Customer customer, OrderItem item) {
    if (!Objects.equals(item.getOrder().getCustomer().getId(), customer.getId())) {
        throw new SecurityException("Không có quyền với mục đơn hàng này");
    }
}

// Gọi trong MỌI method cần check quyền:
public void updateQuantity(Customer customer, Integer itemId, int quantity) {
    OrderItem item = orderItemRepository.findById(itemId).orElseThrow();
    validateOwnership(customer, item);  // lặp lại code kiểm tra
    // ... logic cập nhật số lượng
}
```

**`ReviewService.java`** — check quyền customer:
```java
if (!Objects.equals(review.getUserId(), customer.getId())) {
    throw new SecurityException("Không thể sửa đánh giá của người khác");
}
```

### ⚠️ Vấn đề cụ thể
1. **Vi phạm DRY**: Cùng một logic check quyền (so sánh ID) xuất hiện ở nhiều nơi.
2. **Vi phạm SRP**: Service vừa xử lý nghiệp vụ, vừa lo kiểm tra bảo mật.
3. **Dễ quên**: Khi thêm method mới (VD: xóa mục đơn hàng), lập trình viên dễ quên gọi `validateOwnership()`, gây lỗ hổng bảo mật.

---

## ✅ Giải pháp: Proxy Pattern

Tạo một **Proxy** bao bọc Service thật, tập trung việc kiểm tra quyền và ghi log bảo mật vào một nơi duy nhất:

```java
// ===== Interface =====
public interface CheckoutOperations {
    void updateQuantity(Customer customer, Integer itemId, int quantity);
    void removeItem(Customer customer, Integer itemId);
}

// ===== Real Service (Chỉ lo nghiệp vụ, KHÔNG check quyền) =====
@Service("checkoutOperationsReal")
public class CheckoutOperationsImpl implements CheckoutOperations {
    @Override
    public void updateQuantity(Customer customer, Integer itemId, int quantity) {
        OrderItem item = orderItemRepository.findById(itemId).orElseThrow();
        item.setQuantity(quantity);
        orderItemRepository.save(item);
    }
}

// ===== Proxy (Check quyền tập trung) =====
@Service
@Primary
public class CheckoutOperationsProxy implements CheckoutOperations {
    private final CheckoutOperationsImpl realService;
    private final ReviewRepository reviewRepository;

    @Override
    public void deleteReview(Customer customer, Integer reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        // Kiểm tra quyền sở hữu dựa trên userId (ID-based)
        if (!Objects.equals(review.getUserId(), customer.getId())) {
            throw new SecurityException("Security Violation: Không có quyền truy cập");
        }
        realService.deleteReview(customer, reviewId);
    }
}
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| Check quyền lặp lại 6-7 chỗ | Check tập trung 1 lần trong Proxy |
| Service vừa nghiệp vụ vừa bảo mật | Tách riêng: Proxy (bảo mật) + Real (nghiệp vụ) | 
| Dễ quên check quyền | Proxy tự động bảo vệ mọi lời gọi hàm |
| Khó quản lý log bảo mật | Proxy tự động log mọi truy cập trái phép |
