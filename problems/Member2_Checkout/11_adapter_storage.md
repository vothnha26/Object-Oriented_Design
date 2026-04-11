# 🔴 Bài toán 11: Upload ảnh gắn chặt với Cloudinary

## Patterns: **Adapter** + **Strategy**
## SOLID vi phạm: **DIP** (Dependency Inversion), **OCP**

---

## 📌 Vấn đề hiện tại

Service upload ảnh **gắn cứng** (tightly coupled) vào Cloudinary. Nếu muốn chuyển sang lưu local, Amazon S3, hoặc Firebase Storage → phải **sửa trực tiếp** `CloudinaryService`:

**`CloudinaryService.java`**:
```java
@Service
public class CloudinaryService {
    @Autowired
    private Cloudinary cloudinary; // gắn cứng vào Cloudinary SDK

    public String uploadFile(MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            Object secureUrl = result.get("secure_url");
            Object url = result.get("url");
            return (secureUrl != null ? secureUrl.toString() : (url != null ? url.toString() : null));
        } catch (IOException e) {
            throw new RuntimeException("Lỗi tải ảnh lên Cloudinary", e);
        }
    }
}
```

**Các controller sử dụng trực tiếp `CloudinaryService`**:
```java
// AdminProductController
@Autowired private CloudinaryService cloudinaryService;

String imageUrl = cloudinaryService.uploadFile(imageFile);
product.setImageUrl(imageUrl);
```

### ❌ Vấn đề cụ thể
1. **Vi phạm DIP**: Controller và service layer phụ thuộc vào **implementation cụ thể** (`CloudinaryService`), không phải abstraction
2. **Vi phạm OCP**: Muốn thêm storage provider mới (S3, local, Firebase) → phải sửa service hoặc tạo service mới + sửa tất cả controller
3. **Không thể test**: Khi unit test, upload ảnh thật lên Cloudinary → tốn quota + chậm
4. **Không linh hoạt**: Môi trường dev muốn lưu local, prod muốn lưu Cloudinary → không chuyển đổi được
5. **Chỉ hỗ trợ upload**: Thiếu method delete ảnh cũ khi cập nhật sản phẩm

---

## ✅ Giải pháp: Adapter + Strategy

### Bước 1: Tạo interface trừu tượng (Target)

```java
// ===== Target Interface (contract chung) =====
public interface ImageStorageService {
    /**
     * Upload file và trả về URL public
     */
    String upload(MultipartFile file) throws IOException;

    /**
     * Xóa ảnh theo URL hoặc ID
     */
    void delete(String imageUrl) throws IOException;

    /**
     * Tên provider (cho logging/config)
     */
    String getProviderName();
}
```

### Bước 2: Adapter cho Cloudinary (Adaptee)

```java
// ===== Adapter: Cloudinary → ImageStorageService =====
@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "cloudinary")
public class CloudinaryStorageAdapter implements ImageStorageService {
    private final Cloudinary cloudinary;

    public CloudinaryStorageAdapter(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String upload(MultipartFile file) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(
            file.getBytes(), ObjectUtils.emptyMap());
        Object secureUrl = result.get("secure_url");
        return secureUrl != null ? secureUrl.toString() : null;
    }

    @Override
    public void delete(String imageUrl) throws IOException {
        // Extract public_id từ Cloudinary URL rồi xóa
        String publicId = extractPublicId(imageUrl);
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    @Override
    public String getProviderName() { return "Cloudinary"; }
}
```

### Bước 3: Adapter cho Local Storage (dev/test)

```java
// ===== Adapter: Local File System → ImageStorageService =====
@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageAdapter implements ImageStorageService {
    
    @Value("${storage.local.path:uploads/}")
    private String uploadDir;

    @Override
    public String upload(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path dest = Paths.get(uploadDir, filename);
        Files.createDirectories(dest.getParent());
        Files.write(dest, file.getBytes());
        return "/uploads/" + filename;  // URL tương đối
    }

    @Override
    public void delete(String imageUrl) throws IOException {
        Path path = Paths.get(uploadDir, imageUrl.replace("/uploads/", ""));
        Files.deleteIfExists(path);
    }

    @Override
    public String getProviderName() { return "Local"; }
}
```

### Bước 4: Adapter cho S3 (tương lai)

```java
// ===== Adapter cho AWS S3 (thêm sau mà KHÔNG sửa code cũ → OCP) =====
@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3StorageAdapter implements ImageStorageService {
    // ... implement upload/delete dùng AWS SDK
    @Override public String getProviderName() { return "AWS S3"; }
}
```

### Sử dụng trong controller/service:

```java
// Controller chỉ phụ thuộc vào INTERFACE (DIP)
@Controller
public class AdminProductController {
    private final ImageStorageService storageService; // không phải CloudinaryService

    public AdminProductController(ImageStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/admin/products")
    public String save(Product product, @RequestParam MultipartFile imageFile) {
        if (!imageFile.isEmpty()) {
            String url = storageService.upload(imageFile);  // adapter tự chọn
            product.setImageUrl(url);
        }
    }
}
```

### Cấu hình chuyển đổi qua `application.properties`:

```properties
# Dev: lưu local (nhanh, không cần internet)
storage.provider=local
storage.local.path=uploads/

# Prod: lưu Cloudinary
# storage.provider=cloudinary
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| Gắn cứng Cloudinary | Interface `ImageStorageService` |
| Thêm S3 → sửa code cũ | Thêm `S3StorageAdapter` (OCP) |
| Test upload thật → tốn quota | Test với `LocalStorageAdapter` |
| Dev cần internet | Dev dùng local, prod dùng cloud |
| Không xóa ảnh cũ được | Interface có method `delete()` |
