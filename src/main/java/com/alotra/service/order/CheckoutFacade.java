package com.alotra.service.order;

import com.alotra.dto.CheckoutRequest;
import com.alotra.entity.Customer;
import com.alotra.entity.Order;
import com.alotra.entity.enums.PaymentMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutFacade {
    private final StockService stockService;
    private final OrderFactory orderFactory;
    private final PriceService priceService;
    private final PaymentService paymentService;
    private final CheckoutService checkoutService;

    public CheckoutFacade(StockService stockService,
            OrderFactory orderFactory,
            PriceService priceService,
            PaymentService paymentService,
            CheckoutService checkoutService) {
        this.stockService = stockService;
        this.orderFactory = orderFactory;
        this.priceService = priceService;
        this.paymentService = paymentService;
        this.checkoutService = checkoutService;
    }

    @Transactional
    public Order processCheckout(Customer customer, CheckoutRequest request) {
        stockService.validateStock(request.getCartItems());

        Order order = orderFactory.createOrder(customer, request.getCartItems(), request.getNote());

        priceService.calculateTotal(order, request.getPromotionCode());

        Order savedOrder = checkoutService.saveOrder(order);

        PaymentMethod method = PaymentMethod.fromCode(request.getPaymentMethod());
        if (method == null)
            method = PaymentMethod.CASH;

        paymentService.processPayment(savedOrder, method);

        return savedOrder;
    }
}
