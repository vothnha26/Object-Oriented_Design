import os
import re

dir_path = "problems"

replacements = {
    r'\bDonHang\b': 'Order',
    r'\bKhachHang\b': 'Customer',
    r'\bNhanVien\b': 'Employee',
    r'"ChoXuLy"': 'OrderStatus.PENDING',
    r'"DangPhaChe"': 'OrderStatus.PREPARING',
    r'"DangGiao"': 'OrderStatus.DELIVERING',
    r'"DaGiao"': 'OrderStatus.DELIVERED',
    r'"DaHuy"': 'OrderStatus.CANCELLED',
    r'"ChuaThanhToan"': 'PaymentStatus.UNPAID',
    r'"DaThanhToan"': 'PaymentStatus.PAID',
    r'"TienMat"': 'PaymentMethod.CASH',
    r'"ChuyenKhoan"': 'PaymentMethod.BANK_TRANSFER',
    r'\bGioHang\b': 'Cart',
    r'\bChiTietDonHang\b': 'OrderItem',
    r'\bChiTietGioHang\b': 'CartItem',
    r'\bKhuyenMaiSanPham\b': 'ProductPromotion',
    r'\bSuKienKhuyenMai\b': 'Promotion',
    r'\bSanPham\b': 'Product',
    r'\bDanhGia\b': 'Review',
    r'\bThanhToan\b': 'Payment',
    r'\bString current\b': 'OrderStatus current',
    r'\bString status\b': 'OrderStatus status',
    r'\bString nextStatus\b': 'OrderStatus nextStatus',
    r'\bString orderStatus\b': 'OrderStatus orderStatus',
    r'\bString paymentStatus\b': 'PaymentStatus paymentStatus',
    r'\bString paymentMethod\b': 'PaymentMethod paymentMethod',
    r'\bString currentStatus\b': 'OrderStatus currentStatus'
}

for root, _, files in os.walk(dir_path):
    for file in files:
        if file.endswith(".md"):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            for k, v in replacements.items():
                content = re.sub(k, v, content)
            
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)

print("Translation script executed successfully.")
