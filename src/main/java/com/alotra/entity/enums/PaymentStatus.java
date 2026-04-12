package com.alotra.entity.enums;

public enum PaymentStatus {
    UNPAID,    // Initial state
    PENDING,   // Payment in progress (waiting for gateway confirmation)
    PAID,      // Payment received
    FAILED,    // Payment failed
    REFUNDED   // Payment refunded
}
