package com.alotra.service.interaction;

import com.alotra.entity.Customer;
import com.alotra.entity.OrderItem;
import com.alotra.repository.OrderItemRepository;
import org.springframework.stereotype.Service;

@Service("checkoutOperationsReal")
public class CheckoutOperationsImpl implements CheckoutOperations {
    private final OrderItemRepository orderItemRepository;

    public CheckoutOperationsImpl(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public void updateQuantity(Customer customer, Integer itemId, int quantity) {
        OrderItem item = orderItemRepository.findById(itemId).orElseThrow();
        item.setQuantity(quantity);
        orderItemRepository.save(item);
    }

    @Override
    public void removeItem(Customer customer, Integer itemId) {
        orderItemRepository.deleteById(itemId);
    }
}
