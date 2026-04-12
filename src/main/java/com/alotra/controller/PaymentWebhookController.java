package com.alotra.controller;

import com.alotra.dto.SepayCallbackDTO;
import com.alotra.payment.SepayPaymentProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * REST endpoints for payment processing.
 * - POST /api/payments/initiate — initiate payment (future use with frontend)
 * - POST /api/payments/sepay/callback — webhook from Sepay (public endpoint)
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentWebhookController {
    
    private final SepayPaymentProcessor sepayPaymentProcessor;
    
    public PaymentWebhookController(SepayPaymentProcessor sepayPaymentProcessor) {
        this.sepayPaymentProcessor = sepayPaymentProcessor;
    }
    
    /**
     * Webhook endpoint — Sepay sends callback here after payment.
     * Should be public (no auth) because Sepay needs to reach it from their servers.
     * Always verify signature before processing.
     */
    @PostMapping("/sepay/callback")
    public ResponseEntity<?> handleSepayCallback(@RequestBody Map<String, Object> payload,
                                                 @RequestHeader(value = "X-Sepay-Signature", required = false) String signature) {
        try {
            System.out.println("[Payment] Received Sepay callback: " + payload);
            
            // Convert payload to SepayCallbackDTO
            SepayCallbackDTO callback = new SepayCallbackDTO();
            callback.setMerchantId((String) payload.get("merchantId"));
            callback.setOrderId((String) payload.get("orderId"));
            callback.setTransactionId((String) payload.get("transactionId"));
            callback.setStatus((String) payload.get("status"));
            callback.setSignature(signature);
            
            if (payload.get("amount") instanceof Number) {
                callback.setAmount(((Number) payload.get("amount")).longValue());
            }
            callback.setRaw(payload);
            
            // Verify signature (required for security)
            String payloadStr = payload.toString();
            if (signature != null && !sepayPaymentProcessor.verifyWebhookSignature(payloadStr, signature)) {
                System.err.println("[Payment] Invalid signature — rejecting webhook");
                return ResponseEntity.status(401).body(Map.of("error", "Invalid signature"));
            }
            
            // Process callback (update payment status)
            boolean success = sepayPaymentProcessor.handleSepayCallback(callback);
            
            if (success) {
                return ResponseEntity.ok(Map.of("status", "OK"));
            } else {
                return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process callback"));
            }
            
        } catch (Exception e) {
            System.err.println("[Payment] Error processing webhook: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
    
    /**
     * Health check endpoint for payment service.
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "OK"));
    }
}
