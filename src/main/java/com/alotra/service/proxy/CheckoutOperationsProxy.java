package com.alotra.service.proxy;

import com.alotra.entity.Customer;
import com.alotra.entity.OrderItem;
import com.alotra.repository.OrderItemRepository;
import com.alotra.service.interaction.CheckoutOperations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Primary
public class CheckoutOperationsProxy implements CheckoutOperations {
    private final CheckoutOperations realService;
    private final OrderItemRepository orderItemRepository;

    public CheckoutOperationsProxy(@Qualifier("checkoutOperationsReal") CheckoutOperations realService,
                                   OrderItemRepository orderItemRepository) {
        this.realService = realService;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public void updateQuantity(Customer customer, Integer itemId, int quantity) {
        validateOwnership(customer, itemId);
        realService.updateQuantity(customer, itemId, quantity);
    }

    @Override
    public void removeItem(Customer customer, Integer itemId) {
        validateOwnership(customer, itemId);
        realService.removeItem(customer, itemId);
    }

    private void validateOwnership(Customer customer, Integer itemId) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mục đơn hàng"));
        
        if (!Objects.equals(item.getOrder().getCustomer().getId(), customer.getId())) {
            System.err.println("[SECURITY BREACH] Khách hàng " + customer.getUsername() + 
                               " cố gắng truy cập mục hàng #" + itemId + " không thuộc sở hữu!");
            throw new SecurityException("Bạn không có quyền thao tác trên mục đơn hàng này.");
        }
    }
}
