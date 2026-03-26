package com.alotra.entity.enums;

public enum PromotionStatus {
    INACTIVE(0),
    ACTIVE(1),
    DELETED(2),
    UPCOMING(3),
    EXPIRED(4);

    private final int value;
    PromotionStatus(int value) { this.value = value; }
    public int getValue() { return value; }

    public static PromotionStatus fromValue(int v) {
        for (PromotionStatus s : values()) {
            if (s.value == v) return s;
        }
        return ACTIVE;
    }
}
