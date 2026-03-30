package com.alotra.entity.enums;

public enum ProductStatus {
    INACTIVE(0),
    ACTIVE(1),
    DELETED(2);

    private final int value;
    ProductStatus(int value) { this.value = value; }
    public int getValue() { return value; }

    public static ProductStatus fromValue(int v) {
        for (ProductStatus s : values()) {
            if (s.value == v) return s;
        }
        return ACTIVE;
    }
}
