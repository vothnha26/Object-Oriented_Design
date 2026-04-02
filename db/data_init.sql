-- AloTra Database Initialization Script
-- Sync with Domain Model (Entity/UML)
-- Table names: plural, lowercase, English
-- Column names: snake_case, English

USE `Website_BanTraSua`;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `ordered_toppings`;
TRUNCATE TABLE `order_items`;
TRUNCATE TABLE `payments`;
TRUNCATE TABLE `reviews`;
TRUNCATE TABLE `wishlists`;
TRUNCATE TABLE `orders`;
TRUNCATE TABLE `addresses`;
TRUNCATE TABLE `employees`;
TRUNCATE TABLE `customers`;
TRUNCATE TABLE `promotions`;
TRUNCATE TABLE `product_variants`;
TRUNCATE TABLE `products`;
TRUNCATE TABLE `product_sizes`;
TRUNCATE TABLE `toppings`;
TRUNCATE TABLE `categories`;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Categories
INSERT INTO `categories` (`id`, `name`, `description`) 
VALUES 
(1, 'Trà sữa', 'Các loại trà sữa đặc biệt'),
(2, 'Trà trái cây', 'Trà kết hợp trái cây tươi'),
(3, 'Nước ép', 'Nước ép hoa quả nguyên chất'),
(4, 'Đá bào', 'Thức uống mát lạnh tê tái'),
(5, 'Cà phê', 'Cà phê nguyên chất');

-- 2. Product Sizes
INSERT INTO `product_sizes` (`id`, `name`, `price_adjustment`, `status`)
VALUES
(1, 'S', 0.00, 'ACTIVE'),
(2, 'M', 0.00, 'ACTIVE'),
(3, 'L', 0.00, 'ACTIVE');

-- 3. Toppings
INSERT INTO `toppings` (`id`, `name`, `extra_price`, `status`, `image_url`)
VALUES
(1, 'Trân châu đen', 5000.00, 'ACTIVE', 'https://res.cloudinary.com/dvxxd3vox/image/upload/v1761450324/Tran-Chau-Den_uyp7ow.png'),
(2, 'Trân châu trắng', 6000.00, 'ACTIVE', 'https://res.cloudinary.com/dvxxd3vox/image/upload/v1761450365/tr%C3%A2n_ch%C3%A2u_tr%E1%BA%AFng_nskmtg.jpg'),
(3, 'Thạch dừa 3Q', 4000.00, 'ACTIVE', 'https://res.cloudinary.com/dvxxd3vox/image/upload/v1761450391/th%E1%BA%A1ch_d%E1%BB%ABa_ng5tks.jpg'),
(4, 'Kem Trứng', 10000.00, 'ACTIVE', 'https://res.cloudinary.com/dawrd4avx/image/upload/v1761566324/r4sunw3gbi9jquxig9rp.png');

-- 4. Products
INSERT INTO `products` (`id`, `category_id`, `name`, `description`, `status`, `image_url`)
VALUES
(1, 1, 'Trà Sữa Truyền Thống', 'Trà sữa vị truyền thống', 'ACTIVE', 'https://res.cloudinary.com/dawrd4avx/image/upload/v1761572005/lwl2sjs8wxr9ssxinfwy.png'),
(2, 1, 'Trà Sữa Matcha', 'Trà sữa vị matcha xanh', 'ACTIVE', 'https://res.cloudinary.com/dvxxd3vox/image/upload/v1761449668/Tr%C3%A0_s%E1%BB%AFa_matcha_uwnvw6.png'),
(3, 2, 'Trà Đào Cam Sả', 'Trà đào cam sả thơm mát', 'ACTIVE', 'https://res.cloudinary.com/dvxxd3vox/image/upload/v1761449752/Tr%C3%A0_%C4%91%C3%A0o_cam_s%E1%BA%A3_benjcb.png'),
(4, 3, 'Nước ép cam', 'Nước ép cam tươi', 'ACTIVE', 'https://res.cloudinary.com/dvxxd3vox/image/upload/v1761449818/N%C6%B0%E1%BB%9Bc_%C3%A9p_cam_oyn7hh.png'),
(5, 5, 'Cold Brew trái cây', 'Cà phê dịu nhẹ, mát lạnh, có hương trái cây', 'ACTIVE', 'https://res.cloudinary.com/dvxxd3vox/image/upload/v1761449850/Cold_Drew_tr%C3%A1i_c%C3%A2y_kecbh2.png'),
(7, 5, 'Americano', 'Cà phê nguyên chất', 'ACTIVE', 'https://res.cloudinary.com/dvxxd3vox/image/upload/v1761449892/Americano_ywnwmn.png'),
(8, 2, 'Trà đào', 'Trà olong vị đào', 'ACTIVE', 'https://res.cloudinary.com/dvxxd3vox/image/upload/v1761449960/%C3%94_long_%C4%91%C3%A0o_wwtnis.png'),
(10, 1, 'Trà sữa rang muối', 'Đặc sản mới', 'ACTIVE', 'https://res.cloudinary.com/dawrd4avx/image/upload/v1761566204/j0bknbsqzku94nokujwf.png'),
(15, 1, 'Hồng Trà', 'Nhiều trà ít sữa', 'ACTIVE', 'https://res.cloudinary.com/dawrd4avx/image/upload/v1761566129/cyijqkg0auraqsvu4rff.png'),
(16, 2, 'Trà Kem', 'Kem bùi béo', 'ACTIVE', 'https://res.cloudinary.com/dawrd4avx/image/upload/v1761566273/e6j2lgaxyqmr6hime4kf.png');

-- 5. Product Variants
INSERT INTO `product_variants` (`id`, `product_id`, `size_id`, `price`, `status`)
VALUES
(1, 1, 1, 20000.00, 'ACTIVE'),
(2, 1, 2, 25000.00, 'ACTIVE'),
(3, 1, 3, 30000.00, 'ACTIVE'),
(4, 2, 1, 22000.00, 'ACTIVE'),
(5, 2, 2, 27000.00, 'ACTIVE'),
(6, 2, 3, 32000.00, 'ACTIVE'),
(7, 3, 1, 25000.00, 'ACTIVE'),
(8, 3, 2, 30000.00, 'ACTIVE'),
(9, 3, 3, 35000.00, 'ACTIVE'),
(10, 4, 1, 18000.00, 'ACTIVE'),
(11, 4, 2, 22000.00, 'ACTIVE'),
(12, 4, 3, 27000.00, 'ACTIVE'),
(14, 8, 1, 18000.00, 'ACTIVE'),
(15, 8, 2, 20000.00, 'ACTIVE'),
(16, 8, 3, 22000.00, 'ACTIVE'),
(17, 7, 3, 24000.00, 'ACTIVE'),
(20, 5, 2, 45000.00, 'ACTIVE'),
(21, 10, 1, 25000.00, 'ACTIVE'),
(22, 10, 2, 27000.00, 'ACTIVE'),
(23, 10, 3, 29000.00, 'ACTIVE'),
(28, 15, 1, 20000.00, 'ACTIVE'),
(29, 15, 2, 18000.00, 'ACTIVE'),
(30, 16, 1, 20000.00, 'ACTIVE');

-- 6. Promotions
INSERT INTO `promotions` (`id`, `name`, `code`, `description`, `start_date`, `end_date`, `usage_limit`, `status`, `is_public`)
VALUES
(1, 'Khuyến mãi mùa Noel', 'NOEL2025', 'Mùa Noel, giảm giá đậm sâu', '2025-11-01', '2025-12-31', 100, 'ACTIVE', 1),
(2, 'Năm mới - Tuổi mới', 'NEWYEAR2026', 'Ưu đãi toàn menu', '2026-01-01', '2026-01-10', 200, 'ACTIVE', 1),
(5, 'Valentine', 'VALENTINE', 'Khuyến mãi lễ tình nhân', '2026-02-01', '2026-02-20', 50, 'ACTIVE', 1);

-- 7. Customers (Password: password)
INSERT INTO `customers` (`id`, `username`, `password_hash`, `email`, `full_name`, `phone`, `status`, `created_at`)
VALUES
(1, 'phuoc', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'khanh@example.com', 'Nguyễn Phước Tài', '0912345678', 'ACTIVE', '2025-10-01 00:00:00'),
(2, 'tai', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'tai@example.com', 'Tài Phước', '0987654321', 'ACTIVE', '2025-10-01 00:00:00'),
(3, 'sang', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'dinhsang1105@gmail.com', 'Phan Đình Sáng', '123123123', 'ACTIVE', '2025-10-01 00:00:00'),
(9, 'hoang', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'phuochoa2021vg@gmail.com', 'Hồng Phước Hòa', '12512235', 'ACTIVE', '2025-10-01 00:00:00'),
(10, 'noname012', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'daisyprof205@gmail.com', 'sáng phan', '0123554158', 'ACTIVE', '2025-10-01 00:00:00');

-- 8. Employees (Password: password)
INSERT INTO `employees` (`id`, `username`, `password_hash`, `email`, `full_name`, `phone`, `role`, `status`, `created_at`)
VALUES
(1, 'admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'admin@trasua.com', 'Nguyễn Quản Lý', '0900000001', 'ADMIN', 'ACTIVE', '2025-10-01 00:00:00'),
(3, 'phuochoa', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'hoahp@gmail.com', 'Hong Phuoc Hoa', '0999888777', 'STAFF', 'ACTIVE', '2025-10-01 00:00:00'),
(5, 'noname', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'sangphan@gmail.com', 'Nguyễn A', '5246624422', 'STAFF', 'ACTIVE', '2025-10-01 00:00:00');

-- 9. Addresses
INSERT INTO `addresses` (`id`, `customer_id`, `label`, `address_line`, `is_default`)
VALUES
(1, 1, 'Nhà riêng', '123 Đường ABC, Quận 1, TP.HCM', 1),
(2, 3, 'Công ty', '456 Đường XYZ, Quận Thủ Đức, TP.HCM', 1),
(3, 10, 'Nhà riêng', 'Số 1, Đường Vĩnh Viễn, Quận 10, TP.HCM', 1);

-- 10. Orders
INSERT INTO `orders` (`id`, `customer_id`, `employee_id`, `promotion_id`, `address_id`, `created_at`, `status`)
VALUES
(1, 1, 1, 1, 1, '2025-10-03 12:52:38', 'DELIVERED'),
(2, 3, 3, NULL, 2, '2025-10-05 13:54:38', 'DELIVERED'),
(14, 3, 3, NULL, 2, '2025-10-05 19:03:37', 'DELIVERED');

-- 11. Order Items
INSERT INTO `order_items` (`id`, `order_id`, `variant_id`, `quantity`, `unit_price`, `note`)
VALUES
(1, 1, 2, 2, 25000.00, 'Nóng'),
(2, 1, 5, 1, 27000.00, NULL),
(3, 2, 14, 2, 18000.00, NULL),
(17, 14, 7, 1, 25000.00, NULL);

-- 12. Ordered Toppings
INSERT INTO `ordered_toppings` (`id`, `order_item_id`, `topping_id`, `quantity`, `price`)
VALUES
(1, 1, 1, 1, 5000.00),
(2, 1, 3, 1, 4000.00),
(3, 2, 2, 1, 6000.00);

-- 13. Payments
INSERT INTO `payments` (`id`, `order_id`, `status`, `method`, `amount`, `paid_at`)
VALUES
(1, 1, 'PAID', 'CASH', 59000.00, '2025-10-03 13:00:00'),
(2, 2, 'PAID', 'CASH', 36000.00, '2025-10-05 14:00:00'),
(14, 14, 'PAID', 'CASH', 25000.00, '2025-10-06 10:32:29');

-- 14. Reviews
INSERT INTO `reviews` (`id`, `customer_id`, `product_id`, `order_id`, `stars`, `comment`, `created_at`)
VALUES
(1, 1, 1, 1, 5, 'Rất ngon!', '2025-10-03 13:05:00'),
(2, 3, 8, 2, 5, 'Trà đào thơm lắm', '2025-10-05 14:10:00');

-- 15. Wishlists
INSERT INTO `wishlists` (`id`, `customer_id`, `product_id`, `added_at`)
VALUES
(1, 3, 1, '2025-10-06 09:00:00'),
(2, 3, 3, '2025-10-06 09:05:00');
