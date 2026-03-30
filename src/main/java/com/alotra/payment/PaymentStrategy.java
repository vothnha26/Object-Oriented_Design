package com.alotra.payment;

import com.alotra.entity.Order;

/**
 * Contract for payment method validation and processing.
 * Encapsulates payment-specific logic for different payment methods.
 */
public interface PaymentStrategy {
    
    /**
     * Validate if order can proceed with this payment method.
     * @param order the order to validate
     * @return true if payment is valid and order can proceed, false otherwise
     */
    boolean validatePayment(Order order);
    
    /**
     * Check if this payment method requires pre-payment before delivery.
     * @return true if payment must be received before delivery, false for COD
     */
    boolean requiresPrePayment();
    
    /**
     * Get the payment method name.
     * @return method name (e.g., "Bank Transfer", "Cash")
     */
    String getMethodName();
}
