package com.alotra.entity.enums;

public enum ToppingStatus {
    ACTIVE,
    INACTIVE;

    public static final ToppingStatus UNAVAILABLE = INACTIVE;

    public static ToppingStatus fromValue(Integer val) {
        if (val == null) return ACTIVE;
        if (val == 1) return ACTIVE;
        return INACTIVE;
    }
}
