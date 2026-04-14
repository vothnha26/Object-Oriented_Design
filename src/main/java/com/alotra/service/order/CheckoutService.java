package com.alotra.service.order;

import com.alotra.entity.*;
import com.alotra.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderedToppingRepository orderedToppingRepository;

    public CheckoutService(OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderedToppingRepository orderedToppingRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderedToppingRepository = orderedToppingRepository;
    }

    @Transactional
    public Order saveOrder(Order order) {
        // Lưu Order gốc
        Order savedOrder = orderRepository.save(order);

        // Lưu Payment (đã được liên kết trong Entity)
        if (order.getPayment() != null) {
            order.getPayment().setOrder(savedOrder);
        }

        // Lưu OrderItems và Toppings
        if (order.getItems() != null) {
            for (OrderItem oi : order.getItems()) {
                oi.setOrder(savedOrder);
                OrderItem savedOi = orderItemRepository.save(oi);

                if (oi.getToppings() != null) {
                    for (OrderedTopping ot : oi.getToppings()) {
                        ot.setOrderItem(savedOi);
                        orderedToppingRepository.save(ot);
                    }
                }
            }
        }

        return savedOrder;
    }
}
