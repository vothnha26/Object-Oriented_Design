USE website_bantrasua;
-- ==========================================================
-- DỮ LIỆU MẪU CHO HỆ THỐNG ALOTRA
-- Mật khẩu mặc định cho tất cả user: 123456
-- Ngôn ngữ bảng và field đã được chuẩn hóa lại theo chính xác @Column, @Table trong Entity.
-- ==========================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE Employee;
TRUNCATE TABLE Customer;
TRUNCATE TABLE Category;
TRUNCATE TABLE Product;
TRUNCATE TABLE ProductSize;
TRUNCATE TABLE ProductVariant;
TRUNCATE TABLE Topping;
TRUNCATE TABLE Promotion;
SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================================
-- 1. TẠO TÀI KHOẢN NHÂN VIÊN (Employee)
-- ==========================================================
-- Admin (id: 1)
INSERT INTO Employee (Username, MatKhauHash, Email, TenNV, SoDienThoai, NgayTao, TrangThai, VaiTro) 
VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lH9lBOsl7iKTVKIUi', 'vtn26xn@gmail.com', 'Nguyễn Quản Trị', '0912345678', NOW(), 'ACTIVE', 'ADMIN');

-- Nhân viên (id: 2)
INSERT INTO Employee (Username, MatKhauHash, Email, TenNV, SoDienThoai, NgayTao, TrangThai, VaiTro) 
VALUES 
('staff01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lH9lBOsl7iKTVKIUi', 'vothanhnha26@gmail.com', 'Trần Bán Hàng', '0922345678', NOW(), 'ACTIVE', 'STAFF');

-- Shipper (id: 3)
INSERT INTO Employee (Username, MatKhauHash, Email, TenNV, SoDienThoai, NgayTao, TrangThai, VaiTro) 
VALUES 
('shipper01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lH9lBOsl7iKTVKIUi', 'vothanhnha.mt12@gmail.com', 'Lê Giao Hàng', '0932345678', NOW(), 'ACTIVE', 'SHIPPER');


-- ==========================================================
-- 2. TẠO TÀI KHOẢN KHÁCH HÀNG (Customer)
-- ==========================================================
INSERT INTO Customer (Username, MatKhauHash, Email, TenKH, SoDienThoai, NgayTao, TrangThai) 
VALUES 
('khachhang01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lH9lBOsl7iKTVKIUi', 'vothanhnha152@gmail.com', 'Phạm Khách Hàng', '0942345678', NOW(), 'ACTIVE'),
('khachhang02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lH9lBOsl7iKTVKIUi', '23110277@student.hcmute.edu.vn', 'Võ Mối Quen', '0952345678', NOW(), 'ACTIVE'),
('khachhang03', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lH9lBOsl7iKTVKIUi', 'nhavothanh420@gmail.com', 'Phạm Khách Mới', '0962345678', NOW(), 'ACTIVE');


-- ==========================================================
-- 3. TẠO DANH MỤC SẢN PHẨM (Category)
-- ==========================================================
INSERT INTO Category (MaDM, TenDM, MoTa) VALUES 
(1, 'Cà Phê Truyền Thống', 'Các dòng cà phê phin, đen, sữa truyền thống'),
(2, 'Trà Trái Cây', 'Các loại trà giải nhiệt kết hợp trái cây tươi'),
(3, 'Đá Xay (Freeze)', 'Thức uống đá xay mát lạnh kết hợp kem macchiato');


-- ==========================================================
-- 4. TẠO SẢN PHẨM (Product), SIZE (ProductSize) & BIẾN THỂ (ProductVariant)
-- ==========================================================
-- Tạo Size Sản Phẩm trước (Đã thêm PriceAdjustment)
INSERT INTO ProductSize (Id, Name, PriceAdjustment, Status) VALUES
(1, 'S', 0, 'ACTIVE'),
(2, 'M', 5000, 'ACTIVE'),
(3, 'L', 10000, 'ACTIVE');

-- Sản phẩm 1: Cà Phê Sữa Đá (Danh mục 1)
INSERT INTO Product (MaSP, MaDM, TenSP, MoTa, TrangThai, UrlAnh) VALUES 
(1, 1, 'Cà Phê Sữa Đá', 'Cà phê nguyên chất kết hợp sữa đặc đậm vị', 'ACTIVE', '/images/cf-sua-da.jpg');

-- Biến thể giá cho Cà Phê Sữa Đá
INSERT INTO ProductVariant (ProductId, ProductSizeId, Price, Status) VALUES 
(1, 1, 29000, 'ACTIVE'), -- cf sữa đá size S
(1, 2, 35000, 'ACTIVE'); -- cf sữa đá size M

-- Sản phẩm 2: Trà Đào Cam Sả (Danh mục 2)
INSERT INTO Product (MaSP, MaDM, TenSP, MoTa, TrangThai, UrlAnh) VALUES 
(2, 2, 'Trà Đào Cam Sả', 'Vị thanh ngọt của đào, chua dịu của cam và thơm nồng của sả', 'ACTIVE', '/images/tra-dao-cam-sa.jpg');

-- Biến thể giá cho Trà Đào Cam Sả
INSERT INTO ProductVariant (ProductId, ProductSizeId, Price, Status) VALUES 
(2, 2, 45000, 'ACTIVE'), -- trà đào size M
(2, 3, 55000, 'ACTIVE'); -- trà đào size L


-- ==========================================================
-- 5. TẠO TOPPING (Topping)
-- ==========================================================
INSERT INTO Topping (MaTopping, TenTopping, GiaThem, TrangThai) VALUES 
(1, 'Trân Châu Đen', 10000, 'AVAILABLE'),
(2, 'Trân Châu Trắng', 12000, 'AVAILABLE'),
(3, 'Kem Macchiato', 15000, 'AVAILABLE'),
(4, 'Đào Miếng', 10000, 'AVAILABLE');


-- ==========================================================
-- 6. TẠO KHUYẾN MÃI (Promotion)
-- ==========================================================
INSERT INTO Promotion (MaKM, TenSuKien, MoTa, NgayBD, NgayKT, TrangThai, LuotXem) VALUES 
(1, 'Đồng Giá 29K Cuối Tuần', 'Tất cả đồ uống cà phê đồng giá 29k.', '2026-03-01', '2026-12-31', 'ACTIVE', 100);

-- ### Thông tin đăng nhập Test:

-- | Tài khoản      | Tên Đăng Nhập | Mật Khẩu |
-- |----------------|---------------|----------|
-- | **Admin**      | `admin`       | `123456` |
-- | **Nhân viên**  | `staff01`     | `123456` |
-- | **Shipper**    | `shipper01`   | `123456` |
-- | **Khách hàng 1** | `khachhang01` | `123456` |
-- | **Khách hàng 2** | `khachhang02` | `123456` |
-- | **Khách hàng 3** | `khachhang03` | `123456` |
