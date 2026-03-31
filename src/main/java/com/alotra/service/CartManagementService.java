package com.alotra.service;

import com.alotra.entity.Cart;
import com.alotra.entity.CartItem;
import com.alotra.entity.Customer;
import com.alotra.repository.CartItemRepository;
import com.alotra.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for cart management (create, retrieve, clear carts).
 * Extracted from CartService to achieve Single Responsibility Principle.
 */
@Service
public class CartManagementService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartManagementService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    public Cart getOrCreateActiveCart(Customer customer) {
        return cartRepository.findFirstByCustomerAndStatus(customer, com.alotra.entity.enums.CartStatus.ACTIVE)
            .orElseGet(() -> {
                Cart cart = new Cart();
                cart.setCustomer(customer);
                cart.setStatus(com.alotra.entity.enums.CartStatus.ACTIVE);
                return cartRepository.save(cart);
            });
    }

    public Cart getActiveCart(Customer customer) {
        return cartRepository.findFirstByCustomerAndStatus(customer, com.alotra.entity.enums.CartStatus.ACTIVE).orElse(null);
    }

    @Transactional
    public void clearCart(Customer customer) {
        Cart cart = getActiveCart(customer);
        if (cart != null) {
            cartItemRepository.deleteAll(cartItemRepository.findByCart(cart));
            cart.setStatus(com.alotra.entity.enums.CartStatus.CANCELLED); // Fixed to use CANCELLED
            cartRepository.save(cart);
        }
    }

    @Transactional
    public void deleteCart(Customer customer) {
        Cart cart = getActiveCart(customer);
        if (cart != null) {
            cartItemRepository.deleteAll(cartItemRepository.findByCart(cart));
            cartRepository.delete(cart);
        }
    }

    public int getItemCount(Customer customer) {
        try {
            Cart cart = getActiveCart(customer);
            if (cart == null) return 0;
            return cartItemRepository.findByCart(cart).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        } catch (Exception e) {
            return 0;
        }
    }

    public BigDecimal cartTotal(Customer customer) {
        Cart cart = getActiveCart(customer);
        if (cart == null) return BigDecimal.ZERO;
        
        List<CartItem> items = cartItemRepository.findByCart(cart);
        return items.stream()
            .map(CartItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
