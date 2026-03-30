package com.alotra.service;

import com.alotra.entity.Cart;
import com.alotra.entity.CartItem;
import com.alotra.entity.Customer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Facade for cart operations.
 * Simplifies client interactions by delegating to specialized sub-services.
 * Implements Facade pattern to reduce coupling and provide unified API.
 */
@Service
public class CartFacade {

    private final CartManagementService cartManagementService;
    private final CartItemService cartItemService;

    public CartFacade(CartManagementService cartManagementService,
                      CartItemService cartItemService) {
        this.cartManagementService = cartManagementService;
        this.cartItemService = cartItemService;
    }

    /**
     * Get or create active cart for customer.
     */
    @Transactional
    public Cart getOrCreateActiveCart(Customer customer) {
        return cartManagementService.getOrCreateActiveCart(customer);
    }

    /**
     * Add item with toppings to cart.
     */
    @Transactional
    public CartItem addItemWithOptions(Customer customer, Integer variantId, int qty,
                                       Map<Integer, Integer> toppingQty, String note) {
        return cartItemService.addItemWithOptions(customer, variantId, qty, toppingQty, note);
    }

    /**
     * Update item quantity.
     */
    @Transactional
    public void updateQuantity(Customer customer, Integer itemId, int qty) {
        cartItemService.updateQuantity(customer, itemId, qty);
    }

    /**
     * Change product variant of an item.
     */
    @Transactional
    public void changeVariant(Customer customer, Integer itemId, Integer newVariantId) {
        cartItemService.changeVariant(customer, itemId, newVariantId);
    }

    /**
     * Remove item from cart.
     */
    @Transactional
    public void removeItem(Customer customer, Integer itemId) {
        cartItemService.removeItem(customer, itemId);
    }

    /**
     * List all items in active cart.
     */
    public List<CartItem> listItems(Customer customer) {
        return cartItemService.listItems(customer);
    }

    /**
     * Get total item count in cart.
     */
    public int getItemCount(Customer customer) {
        return cartManagementService.getItemCount(customer);
    }

    /**
     * Get cart total amount.
     */
    public BigDecimal getCartTotal(Customer customer) {
        return cartManagementService.cartTotal(customer);
    }

    /**
     * Clear cart (remove all items).
     */
    @Transactional
    public void clearCart(Customer customer) {
        cartManagementService.clearCart(customer);
    }

    /**
     * Delete cart entirely.
     */
    @Transactional
    public void deleteCart(Customer customer) {
        cartManagementService.deleteCart(customer);
    }
}
