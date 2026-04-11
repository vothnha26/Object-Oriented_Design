# 🔴 Bài toán 13: Bảo mật Wishlist bằng Proxy (Whitelist)

## Pattern: **Proxy**
## SOLID vi phạm: **LSP** (Liskov Substitution), **OCP** (Open/Closed) nếu không dùng Proxy

---

## 📌 Vấn đề hiện tại

Hệ thống cho phép khách hàng thêm bất kỳ sản phẩm nào vào Wishlist, kể cả những sản phẩm đã bị ngưng kinh doanh (`INACTIVE`) hoặc đã bị xóa mềm (`deletedAt != null`). Điều này làm hỏng trải nghiệm người dùng khi họ quay lại danh sách yêu thích và gặp phải các link lỗi hoặc sản phẩm không tồn tại.

Việc thêm logic kiểm tra này trực tiếp vào `WishlistService` sẽ làm phình to service và vi phạm nguyên tắc SRP.

---

## ✅ Giải pháp: Proxy Pattern (Whitelist logic)

Sử dụng Proxy để kiểm soát quyền truy cập và thực hiện **Whitelist** kiểm tra tính hợp lệ của sản phẩm trước khi chuyển yêu cầu đến `WishlistService`.

### Cấu trúc:
- **WishlistOperations (Subject)**: Giao diện chung.
- **WishlistService (Real Subject)**: Thực hiện lưu trữ vào DB.
- **WishlistProxy (Proxy)**: Kiểm tra `product.isAvailable()` trước khi thực thi.

```java
@Service
@Primary
public class WishlistProxy implements WishlistOperations {
    private final WishlistService wishlistService;

    public void addToWishlist(Customer customer, Product product) {
        // Logic Whitelist
        if (product != null && product.isAvailable()) {
            wishlistService.addToWishlist(customer, product);
        } else {
            throw new IllegalStateException("Sản phẩm không khả dụng.");
        }
    }
}
```

## ✅ Lợi ích
1. **Tách biệt logic**: Logic kiểm tra được tách hoàn toàn ra khỏi logic lưu dữ liệu.
2. **Tuân thủ SOLID**: Dễ dàng thay đổi quy tắc Whitelist mà không ảnh hưởng đến code gốc của `WishlistService`.
3. **Trong suốt (Transparent)**: Controller vẫn gọi interface `WishlistOperations` mà không cần biết logic kiểm tra nằm ở Proxy.
