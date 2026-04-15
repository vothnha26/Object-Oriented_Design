package com.alotra.service.order;

import com.alotra.entity.Order;
import com.alotra.entity.Payment;
import com.alotra.entity.enums.PaymentMethod;
import com.alotra.entity.enums.PaymentStatus;
import com.alotra.repository.PaymentRepository;
import com.alotra.repository.OrderRepository;
import com.alotra.service.pricing.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PricingService pricingService;

    @Override
    public void processPayment(Order order, PaymentMethod method) {
        // Reuse existing payment if it already exists (PriceService might have created one to hold the amount)
        Payment payment = order.getPayment();
        if (payment == null) {
            payment = new Payment();
            payment.setOrder(order);
        }
        
        // Ensure final amount is set correctly via PricingService pipeline
        payment.setAmount(pricingService.calculateFinalTotal(order));
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.UNPAID);
        
        paymentRepository.save(payment);
    }

    @Override
    public void markAsPaid(Integer orderId, String transactionRef) {
        orderRepository.findById(orderId).ifPresent(order -> {
            Payment p = order.getPayment();
            if (p != null) {
                p.setStatus(PaymentStatus.PAID);
                p.setTransactionRef(transactionRef);
                p.setPaidAt(LocalDateTime.now());
                paymentRepository.save(p);
            }
        });
    }
}
