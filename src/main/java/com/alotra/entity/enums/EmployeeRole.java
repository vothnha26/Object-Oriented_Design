package com.alotra.entity.enums;

public enum EmployeeRole {
    ADMIN, STAFF;
 
    public static EmployeeRole fromValue(Integer val) {
        if (val == null) return null;
        if (val == 0) return ADMIN;
        if (val == 1) return STAFF;
        return null;
    }
}
