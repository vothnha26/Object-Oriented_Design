package com.alotra.util;

/**
 * Lớp quản lý các Key trong Session.
 * Sử dụng phương thức get để lấy giá trị key, giúp linh hoạt hơn khi cần thay đổi logic đặt tên key.
 */
public final class SessionKeys {
    
    private SessionKeys() {} // Ngăn khởi tạo

    private static final String PREFIX = "ALOTRA_";

    public static String getRegistrationData() {
        return PREFIX + "TEMP_REGISTRATION_DATA";
    }

    public static String getShoppingCart() {
        return PREFIX + "SHOPPING_CART";
    }

    public static String getOtpExpiry() {
        return PREFIX + "OTP_EXPIRY_TIME";
    }
}
