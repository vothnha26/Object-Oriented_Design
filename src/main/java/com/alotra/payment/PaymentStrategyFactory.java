package com.alotra.payment;

import com.alotra.entity.enums.PaymentMethod;
import org.springframework.stereotype.Component;

/**
 * Factory for creating appropriate PaymentStrategy implementations.
 * Maps PaymentMethod enum to corresponding strategy classes.
 */
@Component
public class PaymentStrategyFactory {

    /**
     * Get strategy for the given payment method.
     * @param method the PaymentMethod enum value
     * @return the corresponding PaymentStrategy implementation
     * @throws IllegalArgumentException if method is unsupported
     */
    public PaymentStrategy getStrategy(PaymentMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("Payment method cannot be null");
        }

        return switch (method) {
            case CASH -> new CashPaymentStrategy();
            case BANK_TRANSFER -> new SepayPaymentStrategy();
            case MOMO -> new CashPaymentStrategy(); // Placeholder for Momo
            default -> throw new IllegalArgumentException("Unsupported payment method: " + method);
        };
    }

    /**
     * Get strategy for the given payment method name.
     * @param methodName the name of the payment method (e.g., "CASH", "BANK_TRANSFER")
     * @return the corresponding PaymentStrategy implementation
     */
    public PaymentStrategy getStrategy(String methodName) {
        try {
            PaymentMethod method = PaymentMethod.valueOf(methodName.toUpperCase());
            return getStrategy(method);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown payment method: " + methodName, e);
        }
    }
}
