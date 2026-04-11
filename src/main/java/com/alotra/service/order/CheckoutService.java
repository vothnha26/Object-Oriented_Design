package com.alotra.service.order;

import com.alotra.entity.*;
import com.alotra.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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

    /**
     * @deprecated Dùng CheckoutFacade.processCheckout để quản lý luồng đầy đủ.
     *             Phương thức này chỉ giữ lại để đảm bảo tương thích với code cũ
     *             của Member 1.
     */
    @Deprecated
    @Transactional
    public Order createOrder(Customer customer, Address address, List<OrderItem> items,
            String paymentMethod, String note) {
        Order order = new Order();
        order.setCustomer(customer);
        if (address != null) {
            order.setShippingAddressLine(address.getAddressLine());
        }
        order.setItems(items);

        Payment payment = new Payment();
        payment.setOrder(order);
        order.setPayment(payment);

        return saveOrder(order);
    }
}