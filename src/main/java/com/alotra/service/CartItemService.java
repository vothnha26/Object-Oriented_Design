package com.alotra.service;

import com.alotra.entity.*;
import com.alotra.repository.*;
import com.alotra.discount.DiscountStrategy;
import com.alotra.discount.PercentDiscountStrategy;
import com.alotra.discount.NoDiscountStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service for cart item management (add, update, delete items).
 * Extracted from CartService to achieve Single Responsibility Principle.
 */
@Service
public class CartItemService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final ToppingRepository toppingRepository;
    private final SelectedToppingRepository selectedToppingRepository;
    private final AppliedPromotionRepository appliedPromotionRepository;

    public CartItemService(CartRepository cartRepository,
                          CartItemRepository cartItemRepository,
                          ProductVariantRepository variantRepository,
                          ToppingRepository toppingRepository,
                          SelectedToppingRepository selectedToppingRepository,
                          AppliedPromotionRepository appliedPromotionRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.variantRepository = variantRepository;
        this.toppingRepository = toppingRepository;
        this.selectedToppingRepository = selectedToppingRepository;
        this.appliedPromotionRepository = appliedPromotionRepository;
    }

    @Transactional
    public CartItem addItemWithOptions(Customer customer, Integer variantId, int qty,
                                       Map<Integer, Integer> toppingQty, String note) {
        if (qty <= 0) qty = 1;
        
        Cart cart = getOrCreateActiveCart(customer);
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new IllegalArgumentException("Biến thể không hợp lệ"));
        
        // Apply active product promotion using DiscountStrategy
        BigDecimal basePrice = variant.getPrice();
        Integer discountPercent = (variant.getProduct() != null) 
            ? appliedPromotionRepository.findActiveMaxDiscountPercentForProduct(variant.getProduct().getId()) 
            : null;
        
        DiscountStrategy discount = (discountPercent != null && discountPercent > 0) 
            ? new PercentDiscountStrategy(discountPercent) 
            : new NoDiscountStrategy();
        BigDecimal unitPrice = discount.apply(basePrice);
        
        // Create cart item
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setVariant(variant);
        item.setQuantity(qty);
        item.setUnitPrice(unitPrice);
        item.setNote(note);
        
        // Compute toppings cost per unit
        BigDecimal toppingPerUnit = computeToppingCost(toppingQty);
        BigDecimal lineTotal = unitPrice.add(toppingPerUnit).multiply(BigDecimal.valueOf(qty));
        item.setLineTotal(lineTotal);
        
        item = cartItemRepository.save(item);
        
        // Save topping selections
        persistToppingSelections(item, toppingQty, qty);
        
        return item;
    }

    @Transactional
    public void updateQuantity(Customer customer, Integer itemId, int qty) {
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item không tồn tại"));
        
        validateOwnership(customer, item);
        
        if (qty <= 0) {
            cartItemRepository.delete(item);
            return;
        }
        
        item.setQuantity(qty);
        recomputeLineTotal(item);
        cartItemRepository.save(item);
    }

    @Transactional
    public void changeVariant(Customer customer, Integer itemId, Integer newVariantId) {
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item không tồn tại"));
        
        validateOwnership(customer, item);
        
        ProductVariant target = variantRepository.findById(newVariantId)
            .orElseThrow(() -> new IllegalArgumentException("Biến thể không hợp lệ"));
        
        if (!target.isActive()) {
            throw new IllegalArgumentException("Biến thể đang ngừng bán");
        }
        
        Integer curProductId = item.getVariant().getProduct().getId();
        Integer targetProductId = target.getProduct().getId();
        if (!curProductId.equals(targetProductId)) {
            throw new IllegalArgumentException("Không thể đổi sang sản phẩm khác");
        }
        
        Integer discountPercent = appliedPromotionRepository.findActiveMaxDiscountPercentForProduct(targetProductId);
        DiscountStrategy discount = (discountPercent != null && discountPercent > 0) 
            ? new PercentDiscountStrategy(discountPercent) 
            : new NoDiscountStrategy();
        BigDecimal newUnitPrice = discount.apply(target.getPrice());
        
        item.setVariant(target);
        item.setUnitPrice(newUnitPrice);
        recomputeLineTotal(item);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(Customer customer, Integer itemId) {
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item không tồn tại"));
        
        validateOwnership(customer, item);
        cartItemRepository.delete(item);
    }

    public List<CartItem> listItems(Customer customer) {
        Cart cart = cartRepository.findFirstByCustomerAndStatus(customer, com.alotra.entity.enums.CartStatus.ACTIVE).orElse(null);
        return cart != null ? cartItemRepository.findByCart(cart) : List.of();
    }

    // Helper methods

    private Cart getOrCreateActiveCart(Customer customer) {
        return cartRepository.findFirstByCustomerAndStatus(customer, com.alotra.entity.enums.CartStatus.ACTIVE).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setCustomer(customer);
            cart.setStatus(com.alotra.entity.enums.CartStatus.ACTIVE);
            return cartRepository.save(cart);
        });
    }

    private void validateOwnership(Customer customer, CartItem item) {
        if (!item.getCart().getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Không có quyền truy cập");
        }
    }

    private BigDecimal computeToppingCost(Map<Integer, Integer> toppingQty) {
        BigDecimal total = BigDecimal.ZERO;
        if (toppingQty != null) {
            for (Map.Entry<Integer, Integer> entry : toppingQty.entrySet()) {
                Integer toppingId = entry.getKey();
                Integer quantity = entry.getValue();
                if (quantity != null && quantity > 0) {
                    Topping topping = toppingRepository.findById(toppingId).orElse(null);
                    if (topping != null) {
                        total = total.add(topping.getExtraPrice().multiply(BigDecimal.valueOf(quantity)));
                    }
                }
            }
        }
        return total;
    }

    private void persistToppingSelections(CartItem item, Map<Integer, Integer> toppingQty, int itemQty) {
        if (toppingQty != null) {
            for (Map.Entry<Integer, Integer> entry : toppingQty.entrySet()) {
                Integer toppingId = entry.getKey();
                Integer perUnitQty = entry.getValue();
                if (perUnitQty == null || perUnitQty <= 0) continue;
                
                Topping topping = toppingRepository.findById(toppingId).orElse(null);
                if (topping == null) continue;
                
                SelectedTopping selectedTopping = new SelectedTopping();
                selectedTopping.setCartItem(item);
                selectedTopping.setTopping(topping);
                selectedTopping.setQuantity(perUnitQty * itemQty);
                selectedTopping.setUnitPrice(topping.getExtraPrice());
                selectedTopping.setLineTotal(topping.getExtraPrice().multiply(BigDecimal.valueOf(selectedTopping.getQuantity())));
                
                selectedToppingRepository.save(selectedTopping);
            }
        }
    }

    private void recomputeLineTotal(CartItem item) {
        BigDecimal unitPrice = item.getUnitPrice();
        BigDecimal toppingCost = selectedToppingRepository.findByCartItem(item).stream()
            .map(SelectedTopping::getUnitPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal lineTotal = unitPrice.add(toppingCost).multiply(BigDecimal.valueOf(item.getQuantity()));
        item.setLineTotal(lineTotal);
        cartItemRepository.save(item);
    }
}
