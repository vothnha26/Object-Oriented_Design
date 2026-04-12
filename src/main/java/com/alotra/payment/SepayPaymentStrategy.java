package com.alotra.payment;

import com.alotra.entity.Order;
import com.alotra.entity.enums.PaymentStatus;

/**
 * Sepay payment strategy for online payment gateway.
 * Requires pre-payment; payment verified via webhook callback from Sepay.
 * 
 * Integration flow:
 * 1. User selects SEPAY payment
 * 2. System creates Payment record with PENDING status
 * 3. User redirects to Sepay hosted page (or QR code for bank transfer)
 * 4. After payment, Sepay sends webhook notification
 * 5. System verifies signature and updates Payment to PAID
 * 6. Order can proceed to fulfillment
 */
public class SepayPaymentStrategy implements PaymentStrategy {

    @Override
    public boolean validatePayment(Order order) {
        // For Sepay (online gateway), payment must be PAID before order proceeds
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
        // Sepay payment is initiated via API/gateway, not here
        // This method is called after payment is confirmed via webhook
        System.out.println("[Payment] Sepay payment of " + amount + " confirmed. Ready for fulfillment.");
    }

    @Override
    public String getMethodName() {
        return "Sepay (Online Payment)";
    }
}
