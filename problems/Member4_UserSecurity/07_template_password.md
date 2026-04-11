# 🔴 Bài toán 7: Code chuẩn hóa mật khẩu copy-paste

## Patterns: **Template Method** + **Adapter**
## SOLID vi phạm: **DRY**, **OCP**, **LSP**

---

## 📌 Vấn đề hiện tại

### Vấn đề A: Template Method

Hai class `PasswordHashNormalizer` và `PasswordHashNormalizerNhanVien` có code **gần giống hệt**:

**`PasswordHashNormalizer.java`** (cho `Customer`):
```java
@Override
public void run(ApplicationArguments args) {
    customerRepository.findAll().forEach(customer -> {
        String hash = customer.getPasswordHash();
        if (hash == null) return;
        if (!hash.startsWith("$2a$") && !hash.startsWith("$2b$") && !hash.startsWith("$2y$")) {
            customer.setPasswordHash(encoder.encode(hash));
            customerRepository.save(customer);
        }
    });
}
```

**`PasswordHashNormalizerEmployee.java`** (cho `Employee`):
```java
@Override
public void run(ApplicationArguments args) {
    employeeRepository.findAll().forEach(employee -> {
        String hash = employee.getPasswordHash();
        if (hash == null) return;
        if (!hash.startsWith("$2a$") && !hash.startsWith("$2b$") && !hash.startsWith("$2y$")) {
            employee.setPasswordHash(encoder.encode(hash));
            employeeRepository.save(employee);
        }
    });
}
```

→ **100% logic giống nhau**, chỉ khác entity type (`Customer` vs `Employee`).

### Vấn đề B: Adapter

Custom `PasswordEncoder` trong `SecurityConfig` thực chất là một **Adapter** chuyển đổi giữa nhiều format mật khẩu (BCrypt, noop, plaintext) nhưng được viết dưới dạng **anonymous class 20 dòng**, không tái sử dụng:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new PasswordEncoder() {
        private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        @Override public String encode(CharSequence raw) { return bcrypt.encode(raw); }
        @Override public boolean matches(CharSequence raw, String encoded) {
            // logic check prefix...
            return true;
        }
    };
}
```

---

## ✅ Giải pháp A: Template Method

```java
// ===== Abstract Template =====
public abstract class AbstractPasswordNormalizer<T> implements ApplicationRunner {
    private final PasswordEncoder encoder;

    protected AbstractPasswordNormalizer(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    // Template Method — thuật toán cố định
    @Override
    public final void run(ApplicationArguments args) {
        findAll().forEach(entity -> {
            String hash = getPasswordHash(entity);
            if (hash == null) return;
            if (!isBCrypt(hash)) {
                setPasswordHash(entity, encoder.encode(hash));
                save(entity);
            }
        });
    }

    // Các bước trừu tượng — subclass override
    protected abstract List<T> findAll();
    protected abstract String getPasswordHash(T entity);
    protected abstract void setPasswordHash(T entity, String hash);
    protected abstract void save(T entity);

    // Bước chung — không cần override
    private boolean isBCrypt(String hash) {
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }
}

// ===== Concrete: Customer =====
@Component
public class CustomerPasswordNormalizer extends AbstractPasswordNormalizer<Customer> {
    private final CustomerRepository repository;

    public CustomerPasswordNormalizer(CustomerRepository repository, PasswordEncoder encoder) {
        super(encoder);
        this.repository = repository;
    }

    @Override protected List<Customer> findAll() { return repository.findAll(); }
    @Override protected String getPasswordHash(Customer customer) { return customer.getPasswordHash(); }
    @Override protected void setPasswordHash(Customer customer, String hash) { customer.setPasswordHash(hash); }
    @Override protected void save(Customer customer) { repository.save(customer); }
}

// ===== Concrete: Employee =====
@Component
public class EmployeePasswordNormalizer extends AbstractPasswordNormalizer<Employee> {
    private final EmployeeRepository repository;

    public EmployeePasswordNormalizer(EmployeeRepository repository, PasswordEncoder encoder) {
        super(encoder);
        this.repository = repository;
    }

    @Override protected List<Employee> findAll() { return repository.findAll(); }
    @Override protected String getPasswordHash(Employee employee) { return employee.getPasswordHash(); }
    @Override protected void setPasswordHash(Employee employee, String hash) { employee.setPasswordHash(hash); }
    @Override protected void save(Employee employee) { repository.save(employee); }
}
```

## ✅ Giải pháp B: Adapter Pattern cho PasswordEncoder

```java
// ===== Adapter Class (tách ra riêng, có tên, tái sử dụng) =====
public class LegacyPasswordEncoderAdapter implements PasswordEncoder {
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) return false;
        String stored = encodedPassword.trim();
        String raw = rawPassword == null ? "" : rawPassword.toString();

        // Adapter: chuyển đổi giữa các format khác nhau
        if (stored.startsWith("{bcrypt}")) {
            return bcrypt.matches(raw, stored.substring("{bcrypt}".length()));
        }
        if (stored.startsWith("{noop}")) {
            return raw.equals(stored.substring("{noop}".length()));
        }
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return bcrypt.matches(raw, stored);
        }
        return raw.equals(stored); // legacy plaintext
    }
}

// ===== SecurityConfig sạch hơn =====
@Bean
public PasswordEncoder passwordEncoder() {
    return new LegacyPasswordEncoderAdapter();  // rõ ràng, có tên, testable
}
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| 2 class copy-paste 30+ dòng | 1 abstract + 2 concise subclass |
| Thêm entity mới → copy lần 3 | Thêm entity → extend abstract (OCP) |
| Logic check BCrypt lặp lại | Nằm 1 chỗ trong abstract class |
| Anonymous PasswordEncoder | Adapter class có tên, testable |
