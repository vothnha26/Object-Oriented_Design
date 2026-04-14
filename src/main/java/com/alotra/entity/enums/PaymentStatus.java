package com.alotra.entity.enums;

public enum PaymentStatus {
    PENDING("ChoXacNhan", "Chờ xác nhận"),
    UNPAID("ChuaThanhToan", "Chưa thanh toán"),
    PAID("DaThanhToan", "Đã thanh toán"),
    FAILED("ThatBai", "Thất bại"),
    REFUNDED("DaHoanTien", "Đã hoàn tiền");

    private final String code;
    private final String displayName;

    PaymentStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }

    public static PaymentStatus fromCode(String code) {
        for (PaymentStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }
}
