package com.alotra.entity.enums;

public enum ToppingStatus {
    UNAVAILABLE(0),
    AVAILABLE(1),
    DELETED(2);

    private final int value;
    ToppingStatus(int value) { this.value = value; }
    public int getValue() { return value; }

    public static ToppingStatus fromValue(int v) {
        for (ToppingStatus s : values()) {
            if (s.value == v) return s;
        }
        return AVAILABLE;
    }
}
