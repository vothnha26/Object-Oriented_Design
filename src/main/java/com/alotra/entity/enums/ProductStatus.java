package com.alotra.entity.enums;

public enum ProductStatus {
    ACTIVE,
    INACTIVE,
    OUT_OF_STOCK;

    public static ProductStatus fromValue(Integer val) {
        if (val == null) return ACTIVE;
        if (val == 0) return INACTIVE;
        return ACTIVE;
    }
}
