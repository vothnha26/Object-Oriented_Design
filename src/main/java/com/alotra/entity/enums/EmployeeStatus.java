package com.alotra.entity.enums;

public enum EmployeeStatus {
    ACTIVE,
    INACTIVE;

    public static EmployeeStatus fromValue(Integer val) {
        if (val == null) return null;
        if (val == 1) return ACTIVE;
        if (val == 0) return INACTIVE;
        return null;
    }
}
