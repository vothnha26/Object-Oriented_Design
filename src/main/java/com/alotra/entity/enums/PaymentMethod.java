package com.alotra.entity.enums;

public enum PaymentMethod {
    CASH("TienMat", "Tiền mặt"),
    BANK_TRANSFER("ChuyenKhoan", "Chuyển khoản"),
    MOMO("Momo", "Ví Momo");

    private final String code;
    private final String displayName;

    PaymentMethod(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }

    public static PaymentMethod fromCode(String code) {
        for (PaymentMethod m : values()) {
            if (m.code.equalsIgnoreCase(code)) {
                return m;
            }
        }
        return null;
    }
}
