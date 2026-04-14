package com.alotra.entity.enums;

public enum OrderStatus {
    PENDING("ChoXuLy", "Chờ xử lý"),
    PREPARING("DangPhaChe", "Đang pha chế"),
    DELIVERING("DangGiao", "Đang giao"),
    DELIVERED("DaGiao", "Đã giao"),
    CANCELLED("DaHuy", "Đã hủy");

    private final String code;
    private final String displayName;

    OrderStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }

    public static OrderStatus fromCode(String code) {
        for (OrderStatus s : values()) {
            if (s.code.equalsIgnoreCase(code)) {
                return s;
            }
        }
        return null;
    }
}
