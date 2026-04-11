# 🔴 Bài toán 8: Tạo user phân tán & Cloudinary Singleton

## Patterns: **Factory** + **Singleton**
## SOLID vi phạm: **SRP**, **DIP**

---

## 📌 Vấn đề hiện tại

### Vấn đề A: Tạo User phân tán (cần Factory)

Hệ thống có **2 loại user** (`Customer`, `Employee`) nhưng không có factory thống nhất. Logic tạo user nằm **rải rác**:

**`CreateAdminInitializer.java`** — tạo Customer thủ công:
```java
Customer admin = new Customer();
admin.setUsername("boss");
admin.setFullName("Cinema Administrator");
admin.setEmail("boss@cinema.com");
admin.setPhone("0900000000");
admin.setStatus(CustomerStatus.ACTIVE);
admin.setPasswordHash(encoder.encode("123"));
customerRepository.save(admin);
```

**`RegistrationController`** — cũng tạo Customer thủ công với logic khác.

**`UsersAdminController`** — tạo Employee qua `EmployeeService.saveHandlingPassword()`.

→ **Không có nơi tập trung** logic tạo user. Nếu thêm field mới (VD: `avatar`) → phải sửa mọi nơi tạo user.

### Vấn đề B: Cloudinary instance (cần Singleton rõ ràng)

`CloudinaryConfig` tạo bean Cloudinary nhưng Spring quản lý:
```java
@Bean
public Cloudinary cloudinary() {
    return new Cloudinary(Map.of(
        "cloud_name", cloudName,
        "api_key", apiKey,
        "api_secret", apiSecret
    ));
}
```

Vấn đề: code không rõ ràng đây là Singleton. `CloudinaryService` dùng `@Autowired` field injection thay vì constructor injection → khó test, **vi phạm DIP**.

---

## ✅ Giải pháp A: Factory Pattern cho User

```java
// ===== Interface chung cho User =====
public interface UserAccount {
    String getUsername();
    String getEmail();
    String getPasswordHash();
}

// ===== Factory =====
public class UserFactory {

    private final PasswordEncoder encoder;

    public UserFactory(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    // Tạo khách hàng (chưa kích hoạt, cần OTP)
    public Customer createCustomer(String username, String email, String fullName,
                                     String phone, String plainPassword) {
        validateCommon(username, email, plainPassword);
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setEmail(email);
        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setStatus(CustomerStatus.PENDING); // chưa kích hoạt
        customer.setPasswordHash(encoder.encode(plainPassword));
        return customer;
    }

    // Tạo admin
    public Customer createAdmin(String username, String email, String plainPassword) {
        Customer admin = createCustomer(username, email, "Administrator", null, plainPassword);
        admin.setStatus(CustomerStatus.ACTIVE); // admin kích hoạt ngay
        return admin;
    }

    // Tạo nhân viên (vendor/shipper)
    public Employee createEmployee(String username, String email, String fullName,
                                    String phone, EmployeeRole role, String plainPassword) {
        validateCommon(username, email, plainPassword);
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setEmail(email);
        employee.setFullName(fullName);
        employee.setPhone(phone);
        employee.setRole(role);
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setPasswordHash(encoder.encode(plainPassword));
        return employee;
    }

    private void validateCommon(String username, String email, String password) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username không được trống");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email không được trống");
        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("Mật khẩu phải >= 6 ký tự");
    }
}
```

## ✅ Giải pháp B: Singleton Pattern rõ ràng cho Cloudinary

```java
// ===== Singleton Configuration =====
@Configuration
public class CloudinaryConfig {
    @Value("${cloudinary.cloud_name}") private String cloudName;
    @Value("${cloudinary.api_key}") private String apiKey;
    @Value("${cloudinary.api_secret}") private String apiSecret;

    @Bean  // Spring Singleton scope (default)
    public Cloudinary cloudinary() {
        return new Cloudinary(Map.of(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        ));
    }
}

// ===== Service dùng constructor injection (DIP) =====
@Service
public class CloudinaryService {
    private final Cloudinary cloudinary; // final → Singleton rõ ràng

    // Constructor injection thay vì @Autowired field
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) { /* logic upload */ return "url"; }
}
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| Tạo user rải rác 3+ nơi | `UserFactory` tập trung |
| Thiếu validation khi tạo user | Factory validate đồng nhất |
| `@Autowired` field injection | Constructor injection (testable) |
| Singleton ẩn trong Spring | Singleton rõ ràng qua `@Bean` + `final` |
