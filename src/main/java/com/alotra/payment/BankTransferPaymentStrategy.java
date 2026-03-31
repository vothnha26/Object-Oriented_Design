package com.alotra.payment;

import com.alotra.entity.Order;
import com.alotra.entity.enums.PaymentStatus;

/**
 * Bank Transfer payment strategy.
 * Requires payment to be received (PAID status) before order can proceed.
 */
public class BankTransferPaymentStrategy implements PaymentStrategy {

    @Override
    public boolean validatePayment(Order order) {
        // For bank transfer, payment must be PAID before delivery
        if (order == null || order.getPayment() == null) {
            return false;
        }
        
        PaymentStatus paymentStatus = order.getPayment().getStatus();
        return paymentStatus == PaymentStatus.PAID;
    }

    @Override
    public boolean requiresPrePayment() {
        return true;
    }

    @Override
    public void processPayment(java.math.BigDecimal amount) {
        // For bank transfer, we might trigger an external API or wait for notification
        System.out.println("[Payment] Processing bank transfer of " + amount + ". Waiting for confirmation.");
    }

    @Override
    public String getMethodName() {
        return "Bank Transfer";
    }
}
