package com.alotra.service.order;

import com.alotra.entity.*;
import com.alotra.entity.enums.PaymentMethod;
import com.alotra.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CheckoutService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderedToppingRepository orderedToppingRepository;
    private final AddressRepository addressRepository;

    public CheckoutService(OrderRepository orderRepository,
                          OrderItemRepository orderItemRepository,
                          OrderedToppingRepository orderedToppingRepository,
                          AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderedToppingRepository = orderedToppingRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public Order createOrder(Customer customer, Address address, List<OrderItem> items,
                            String paymentMethod, String note) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Chưa có sản phẩm để đặt hàng");
        }

        // Create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setAddress(address);

        // Set payment
        Payment payment = new Payment();
        if (paymentMethod != null) {
            try {
                payment.setMethod(PaymentMethod.valueOf(paymentMethod.toUpperCase()));
            } catch (Exception ignored) {}
        }
        payment.setOrder(order);
        payment.setAmount(BigDecimal.ZERO); // To be updated
        order.setPayment(payment);

        order = orderRepository.save(order);

        // Persist order items with toppings
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem oi : items) {
            oi.setOrder(order);
            OrderItem savedOi = orderItemRepository.save(oi);
            
            for (OrderedTopping ot : oi.getToppings()) {
                ot.setOrderItem(savedOi);
                orderedToppingRepository.save(ot);
            }
            totalAmount = totalAmount.add(oi.getLineTotal());
        }

        payment.setAmount(totalAmount);
        return orderRepository.save(order);
    }
}
