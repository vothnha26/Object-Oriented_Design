# 🔴 Bài toán 8: Tạo user phân tán & Quản lý OTP tạm thời

## Patterns: **Factory** + **Singleton**
## SOLID vi phạm: **SRP**, **DIP**

---

## 📌 Vấn đề hiện tại

### Vấn đề A: Tạo User phân tán (cần Factory)
Hệ thống có các loại user (`Customer`, `Employee`) nhưng logic khởi tạo nằm rải rác ở nhiều Controller và Initializer, gây khó khăn khi cần thay đổi cấu trúc dữ liệu hoặc quy trình đăng ký.

### Vấn đề B: Xác thực OTP không cần Database
Trước đây mã OTP được lưu trực tiếp vào bảng User. Việc này làm phình to Database và không an toàn. Khi bỏ trường `otpCode` trong DB, hệ thống cần một cơ chế lưu trữ tạm thời (Short-lived) để xác thực.

---

## ✅ Giải pháp A: Factory Pattern cho User

Tập trung toàn bộ logic khởi tạo và mã hóa mật khẩu vào `UserFactory`.

```java
@Component
public class UserFactory {
    private final PasswordEncoder encoder;

    public UserFactory(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public Customer createPendingCustomer(String username, String email, String fullName, String phone, String password) {
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setEmail(email);
        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setPasswordHash(encoder.encode(password));
        customer.setStatus(CustomerStatus.INACTIVE); // Đợi xác thực OTP
        return customer;
    }
}
```

## ✅ Giải pháp B: OTP qua Session (Web-layer)

Thay vì lưu mã xác thực vào thực thể `User`, ta sử dụng **HttpSession** để lưu trữ tạm thời.

```java
// Trong RegistrationController
@PostMapping("/register")
public String handleRegister(HttpSession session, @RequestParam String email) {
    String otp = OtpGenerator.generate();
    // Lưu OTP vào session thay vì DB
    session.setAttribute("REGISTER_OTP_" + email, otp);
    session.setMaxInactiveInterval(300); // Hết hạn sau 5 phút
    
    emailService.sendOtp(email, otp);
    return "redirect:/verify";
}

@PostMapping("/verify")
public String verifyOtp(HttpSession session, @RequestParam String email, @RequestParam String userOtp) {
    String sessionOtp = (String) session.getAttribute("REGISTER_OTP_" + email);
    if (userOtp.equals(sessionOtp)) {
        customerService.activateUser(email);
        session.removeAttribute("REGISTER_OTP_" + email); // Xóa sau khi dùng
        return "success";
    }
    return "error";
}
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| OTP lưu vĩnh viễn trong DB | Lưu tạm trong RAM (Session), tự xóa khi hết hạn |
| Logic tạo user rải rác | `UserFactory` quản lý tập trung |
| DB bị phình to bởi rác OTP | Database sạch sẽ, chỉ lưu User thật |
