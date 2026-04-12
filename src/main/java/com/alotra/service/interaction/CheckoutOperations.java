package com.alotra.service.interaction;

import com.alotra.entity.Customer;

public interface CheckoutOperations {
    void updateQuantity(Customer customer, Integer itemId, int quantity);
    void removeItem(Customer customer, Integer itemId);
}
