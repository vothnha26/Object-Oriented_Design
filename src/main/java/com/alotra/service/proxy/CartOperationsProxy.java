package com.alotra.service.proxy;

import com.alotra.entity.*;
import com.alotra.repository.CartItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Primary
public class CartOperationsProxy implements CartOperations {
    private static final Logger log = LoggerFactory.getLogger(CartOperationsProxy.class);

    private final CartOperations real;
    private final CartItemRepository cartItemRepository;

    public CartOperationsProxy(@Qualifier("cartOperationsReal") CartOperations real,
                               CartItemRepository cartItemRepository) {
        this.real = real;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public Cart getOrCreateActiveCart(Customer customer) {
        return real.getOrCreateActiveCart(customer);
    }

    @Override
    public CartItem addItemWithOptions(Customer customer, Integer variantId, int qty, Map<Integer, Integer> toppingQty, String note) {
        return real.addItemWithOptions(customer, variantId, qty, toppingQty, note);
    }

    @Override
    public List<CartItem> listItems(Customer customer) {
        return real.listItems(customer);
    }

    @Override
    public int getItemCount(Customer customer) {
        return real.getItemCount(customer);
    }

    @Override
    public void updateQuantity(Customer customer, Integer itemId, int qty) {
        validateOwnership(customer, itemId, "updateQuantity");
        real.updateQuantity(customer, itemId, qty);
    }

    @Override
    public void removeItem(Customer customer, Integer itemId) {
        validateOwnership(customer, itemId, "removeItem");
        real.removeItem(customer, itemId);
    }

    @Override
    public BigDecimal calcTotal(List<CartItem> items) {
        return real.calcTotal(items);
    }

    @Override
    public Order checkoutWithOptions(Customer customer, List<Integer> itemIds, String paymentMethod,
                                     String note, String receivingMethod,
                                     String shipName, String shipPhone, String shipAddress) {
        return real.checkoutWithOptions(customer, itemIds, paymentMethod, note, receivingMethod, shipName, shipPhone, shipAddress);
    }

    @Override
    public List<Topping> listActiveToppings() {
        return real.listActiveToppings();
    }

    @Override
    public Map<Integer, List<SelectedTopping>> getToppingsForItems(List<CartItem> items) {
        return real.getToppingsForItems(items);
    }

    @Override
    public void updateToppings(Customer customer, Integer itemId, Map<Integer, Integer> toppingQtyById) {
        validateOwnership(customer, itemId, "updateToppings");
        real.updateToppings(customer, itemId, toppingQtyById);
    }

    @Override
    public void changeVariant(Customer customer, Integer itemId, Integer newVariantId) {
        validateOwnership(customer, itemId, "changeVariant");
        real.changeVariant(customer, itemId, newVariantId);
    }

    @Override
    public List<ProductVariant> listVariantsForProduct(Product product) {
        return real.listVariantsForProduct(product);
    }

    private void validateOwnership(Customer customer, Integer itemId, String action) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow();
        Integer ownerId = item.getCart() != null && item.getCart().getCustomer() != null
                ? item.getCart().getCustomer().getId()
                : null;
        if (!Objects.equals(ownerId, customer.getId())) {
            log.warn("SECURITY: user {} attempted {} on cart item {} owned by {}",
                    customer.getId(), action, itemId, ownerId);
            throw new SecurityException("Không có quyền với mục giỏ hàng này");
        }
    }
}