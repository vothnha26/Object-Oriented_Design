package com.alotra.service.order;

import com.alotra.dto.CheckoutRequest;
import com.alotra.entity.*;
import com.alotra.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutFacade {
    private final StockService stockService;
    private final OrderFactory orderFactory;
    private final PriceService priceService;
    private final PaymentService paymentService;
    private final CheckoutService checkoutService;
    private final AddressRepository addressRepository;

    public CheckoutFacade(StockService stockService, OrderFactory orderFactory,
                          PriceService priceService, PaymentService paymentService,
                          CheckoutService checkoutService, AddressRepository addressRepository) {
        this.stockService = stockService;
        this.orderFactory = orderFactory;
        this.priceService = priceService;
        this.paymentService = paymentService;
        this.checkoutService = checkoutService;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public Order processCheckout(Customer customer, CheckoutRequest request) {
        // 1. Kiểm tra tồn kho (Stock Service)
        stockService.validateStock(request.getCartItems());

        // 2. Chuyển đổi DTO sang Entity (Order Factory)
        Address address = addressRepository.findById(request.getAddressId())
                .orElse(null);
                
        // Ghi chú (Note) được gán thông qua OrderFactory cho các OrderItem (theo class_diagram.puml)
        Order order = orderFactory.createOrder(customer, address, request.getCartItems(), request.getNote());

        // 3. Tính toán giá và khuyến mãi (Price Service)
        priceService.calculateTotal(order, request.getPromotionCode());

        // 4. Xử lý thanh toán (Payment Service)
        paymentService.processPayment(order, request.getPaymentMethod());

        // 5. Lưu đơn hàng thông qua Checkout Service chuyên biệt
        return checkoutService.saveOrder(order);
    }
}
