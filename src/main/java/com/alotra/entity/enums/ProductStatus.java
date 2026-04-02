package com.alotra.entity.enums;

public enum ProductStatus {
    ACTIVE,
    INACTIVE,
    OUT_OF_STOCK;

    public static ProductStatus fromValue(Integer val) {
        if (val == null) return ACTIVE;
        if (val == 1) return ACTIVE;
        if (val == 0) return INACTIVE;
        if (val == -1) return OUT_OF_STOCK;
        return ACTIVE;
    }
}
