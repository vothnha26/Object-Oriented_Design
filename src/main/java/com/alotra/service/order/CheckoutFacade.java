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
        // 1. Kiểm tra tồn kho
        stockService.validateStock(request.getCartItems());

        // 2. Chuyển đổi DTO sang Entity
        Order order;
        if (request.getShippingAddress() != null && !request.getShippingAddress().isBlank()) {
            // Trường hợp khách nhập địa chỉ mới (dạng String)
            order = orderFactory.createOrder(customer, null, request.getCartItems(), request.getNote());
            order.setShippingAddressLine(request.getShippingAddress());
        } else {
            // Trường hợp khách chọn địa chỉ có sẵn (dạng ID)
            Address address = addressRepository.findById(request.getAddressId()).orElse(null);
            order = orderFactory.createOrder(customer, address, request.getCartItems(), request.getNote());
        }

        // 3. Tính toán giá
        priceService.calculateTotal(order, request.getPromotionCode());

        // 4. Xử lý thanh toán
        paymentService.processPayment(order, request.getPaymentMethod());

        // 5. Lưu đơn hàng
        return checkoutService.saveOrder(order);
    }
}
