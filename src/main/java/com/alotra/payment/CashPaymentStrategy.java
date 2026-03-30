package com.alotra.payment;

import com.alotra.entity.Order;

/**
 * Cash on Delivery payment strategy.
 * No pre-payment required; payment received at delivery.
 */
public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public boolean validatePayment(Order order) {
        // For COD, any order can proceed receipt doesn't matter
        // Just ensure order and payment object exist
        return order != null && order.getPayment() != null;
    }

    @Override
    public boolean requiresPrePayment() {
        return false;
    }

    @Override
    public String getMethodName() {
        return "Cash on Delivery";
    }
}
