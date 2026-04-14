package com.alotra.entity.enums;

public enum CustomerStatus {
    ACTIVE,
    PENDING,
    INACTIVE,
    BANNED;

    public static CustomerStatus fromValue(Integer val) {
        if (val == null) return null;
        if (val == 1) return ACTIVE;
        if (val == 2) return PENDING;
        if (val == 0) return INACTIVE;
        if (val == -1) return BANNED;
        return null;
    }
}
