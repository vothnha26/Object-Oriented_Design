package com.alotra.payment;

import com.alotra.config.SepayConfig;
import com.alotra.dto.PaymentRequest;
import com.alotra.dto.PaymentResult;
import com.alotra.dto.SepayCallbackDTO;
import com.alotra.entity.Order;
import com.alotra.entity.Payment;
import com.alotra.entity.enums.PaymentStatus;
import com.alotra.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.*;

/**
 * Sepay payment processor for handling API calls, webhook callbacks, and signature verification.
 * Implements business logic for:
 * - Creating payment requests to Sepay API
 * - Verifying webhook signatures (HMAC-SHA256)
 * - Updating payment status based on callback
 * - Generating redirect URLs or QR codes
 */
@Service
public class SepayPaymentProcessor {
    
    private final SepayConfig sepayConfig;
    private final PaymentRepository paymentRepository;
    
    public SepayPaymentProcessor(SepayConfig sepayConfig, PaymentRepository paymentRepository) {
        this.sepayConfig = sepayConfig;
        this.paymentRepository = paymentRepository;
    }
    
    /**
     * Initiate a payment with Sepay — create Payment record and generate redirect URL/QR.
     * In real scenario, this would call Sepay API to get QR code or hosted checkout URL.
     * For MVP, this demonstrates the structure and signature logic.
     */
    public PaymentResult initiateSepayPayment(PaymentRequest request, Order order) {
        PaymentResult result = new PaymentResult();
        
        try {
            // Validate input
            if (order == null || request.getAmount() == null) {
                result.setSuccess(false);
                result.setMessage("Invalid payment request");
                return result;
            }
            
            // Create Payment record with PENDING status
            Payment payment = order.getPayment();
            if (payment == null) {
                payment = new Payment();
                payment.setOrder(order);
            }
            payment.setStatus(PaymentStatus.PENDING);
            payment.setAmount(request.getAmount());
            payment.setMethod(order.getPayment() != null ? order.getPayment().getMethod() : null);
            
            // Generate transaction reference (unique identifier for Sepay)
            String transactionRef = "ALOTRA_" + order.getId() + "_" + System.currentTimeMillis();
            payment.setTransactionRef(transactionRef);
            
            Payment savedPayment = paymentRepository.save(payment);
            
            // In real implementation: call Sepay API to get QR code or hosted URL
            // For now, we construct a mock redirect URL
            String redirectUrl = buildSepayRedirectUrl(savedPayment);
            
            result.setSuccess(true);
            result.setMessage("Payment initiated successfully");
            result.setRedirectUrl(redirectUrl);
            result.setTransactionRef(transactionRef);
            
            return result;
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Failed to initiate payment: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * Verify webhook callback signature using HMAC-SHA256.
     * Sepay sends: signature = HMAC-SHA256(webhookSecret, payload)
     */
    public boolean verifyWebhookSignature(String payload, String receivedSignature) {
        try {
            String computedSignature = generateHmacSignature(payload, sepayConfig.getWebhookSecret());
            return computedSignature.equalsIgnoreCase(receivedSignature);
        } catch (Exception e) {
            System.err.println("[Sepay] Signature verification failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Handle webhook callback from Sepay and update payment status.
     */
    public boolean handleSepayCallback(SepayCallbackDTO callback) {
        try {
            // Extract transaction reference from callback (Sepay includes it in payload)
            String orderId = callback.getOrderId();
            String status = callback.getStatus();
            
            if (orderId == null || status == null) {
                System.err.println("[Sepay] Invalid callback: missing orderId or status");
                return false;
            }
            
            // Find payment by order ID
            Optional<Order> orderOpt = Optional.empty();
            // In real scenario: orderRepository.findById(Integer.parseInt(orderId))
            // For now, simplified lookup
            
            if (orderOpt.isEmpty()) {
                System.err.println("[Sepay] Order not found: " + orderId);
                return false;
            }
            
            Order order = orderOpt.get();
            Payment payment = order.getPayment();
            
            if (payment == null) {
                System.err.println("[Sepay] Payment not found for order: " + orderId);
                return false;
            }
            
            // Map Sepay status to PaymentStatus
            // Assume Sepay returns: "00" = success, "01" = pending, "02" = failed, etc.
            if ("00".equals(status) || "success".equalsIgnoreCase(status)) {
                payment.setStatus(PaymentStatus.PAID);
            } else if ("01".equals(status) || "pending".equalsIgnoreCase(status)) {
                payment.setStatus(PaymentStatus.PENDING);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
            }
            
            // Update transaction reference if provided in callback
            if (callback.getTransactionId() != null) {
                payment.setTransactionRef(callback.getTransactionId());
            }
            
            paymentRepository.save(payment);
            
            System.out.println("[Sepay] Callback processed: orderId=" + orderId + ", newStatus=" + payment.getStatus());
            return true;
            
        } catch (Exception e) {
            System.err.println("[Sepay] Error processing callback: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Build VietQR URL for bank transfer QR code generation.
     * Returns a QR image URL from vietqr.io API with bank details, amount, and order info.
     * Format: https://img.vietqr.io/image/{bankCode}-{accountNumber}-print.png?amount={amount}&addInfo={description}
     */
    private String buildSepayRedirectUrl(Payment payment) {
        try {
            // Generate order reference for VietQR display
            Order order = payment.getOrder();
            String addInfo = "ALOTRA DH " + order.getId();
            
            // Encode the addInfo parameter for URL
            String encodedInfo = URLEncoder.encode(addInfo, StandardCharsets.UTF_8);
            
            // Get payment amount (convert to long for VietQR)
            long amount = payment.getAmount() != null ? payment.getAmount().longValue() : 0L;
            
            // Build VietQR URL
            String vietQrUrl = String.format(
                "https://img.vietqr.io/image/%s-%s-print.png?amount=%d&addInfo=%s",
                sepayConfig.getBankCode(),
                sepayConfig.getBankAccount(),
                amount,
                encodedInfo
            );
            
            System.out.println("[Sepay] Generated VietQR URL: " + vietQrUrl);
            return vietQrUrl;
            
        } catch (Exception e) {
            System.err.println("[Sepay] Failed to generate VietQR URL: " + e.getMessage());
            // Fallback to basic Sepay checkout URL
            String baseUrl = sepayConfig.getPublicBaseUrl() + "/sepay-checkout";
            return baseUrl + "?paymentId=" + payment.getId();
        }
    }
    
    /**
     * Generate HMAC-SHA256 signature for request/callback verification.
     */
    private String generateHmacSignature(String message, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
