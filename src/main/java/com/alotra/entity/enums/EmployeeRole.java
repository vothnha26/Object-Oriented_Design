package com.alotra.entity.enums;

public enum EmployeeRole {
    ADMIN,
    STAFF,
    SHIPPER;

    public static EmployeeRole fromValue(Integer val) {
        if (val == null) return null;
        if (val == 0) return ADMIN;
        if (val == 1) return STAFF;
        if (val == 2) return SHIPPER;
        return null;
    }
}
