package com.alotra.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alotra.entity.Cart;
import com.alotra.entity.CartItem;
import com.alotra.entity.Customer;
import com.alotra.entity.Order;
import com.alotra.entity.Product;
import com.alotra.entity.ProductVariant;
import com.alotra.entity.SelectedTopping;
import com.alotra.entity.Topping;
import com.alotra.repository.ProductVariantRepository;
import com.alotra.repository.SelectedToppingRepository;
import com.alotra.repository.ToppingRepository;

/**
 * Lightweight wrapper service for cart operations.
 * Delegates to specialized sub-services (CartFacade, CheckoutService).
 * Maintains backward compatibility for existing code.
 */
@Service
public class CartService {

    private final CartFacade cartFacade;
    private final CheckoutService checkoutService;
    private final ToppingRepository toppingRepository;
    private final SelectedToppingRepository selectedToppingRepository;
    private final ProductVariantRepository variantRepository;

    public CartService(CartFacade cartFacade,
                      CheckoutService checkoutService,
                      ToppingRepository toppingRepository,
                      SelectedToppingRepository selectedToppingRepository,
                      ProductVariantRepository variantRepository) {
        this.cartFacade = cartFacade;
        this.checkoutService = checkoutService;
        this.toppingRepository = toppingRepository;
        this.selectedToppingRepository = selectedToppingRepository;
        this.variantRepository = variantRepository;
    }

    // Delegate to CartFacade
    
    @Transactional
    public Cart getOrCreateActiveCart(Customer customer) {
        return cartFacade.getOrCreateActiveCart(customer);
    }

    @Transactional
    public CartItem addItemWithOptions(Customer customer, Integer variantId, int qty,
                                       Map<Integer, Integer> toppingQty, String note) {
        return cartFacade.addItemWithOptions(customer, variantId, qty, toppingQty, note);
    }

    public List<CartItem> listItems(Customer customer) {
        return cartFacade.listItems(customer);
    }

    public int getItemCount(Customer customer) {
        return cartFacade.getItemCount(customer);
    }

    @Transactional
    public void updateQuantity(Customer customer, Integer itemId, int qty) {
        cartFacade.updateQuantity(customer, itemId, qty);
    }

    @Transactional
    public void removeItem(Customer customer, Integer itemId) {
        cartFacade.removeItem(customer, itemId);
    }

    @Transactional
    public void changeVariant(Customer customer, Integer itemId, Integer newVariantId) {
        cartFacade.changeVariant(customer, itemId, newVariantId);
    }

    @Transactional
    public void clearCart(Customer customer) {
        cartFacade.clearCart(customer);
    }

    // Delegate to CheckoutService

    public BigDecimal calcTotal(List<CartItem> items) {
        return checkoutService.calculateTotal(items);
    }

    public Map<Integer, List<SelectedTopping>> getToppingsForItems(List<CartItem> items) {
        return checkoutService.getToppingsForItems(items);
    }

    @Transactional
    public Order checkoutWithOptions(Customer customer, List<Integer> itemIds, String paymentMethod,
                                     String note, String receivingMethod,
                                     String shipName, String shipPhone, String shipAddress) {
        Cart activeCart = cartFacade.getOrCreateActiveCart(customer);
        return checkoutService.checkoutWithOptions(customer, activeCart, itemIds, paymentMethod,
                                                   note, receivingMethod, shipName, shipPhone, shipAddress);
    }

    // Topping management (simple delegation)

    public List<Topping> listActiveToppings() {
        return toppingRepository.findByDeletedAtIsNull();
    }

    @Transactional
    public void updateToppings(Customer customer, Integer itemId, Map<Integer, Integer> toppingQtyById) {
        CartItem item = cartFacade.listItems(customer).stream()
            .filter(ci -> ci.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Item không tồn tại"));

        // Delete old toppings
        List<SelectedTopping> oldToppings = selectedToppingRepository.findByCartItem(item);
        for (SelectedTopping ot : oldToppings) {
            selectedToppingRepository.delete(ot);
        }

        // Add new toppings
        if (toppingQtyById != null) {
            BigDecimal newToppingCost = BigDecimal.ZERO;
            for (Map.Entry<Integer, Integer> entry : toppingQtyById.entrySet()) {
                Integer toppingId = entry.getKey();
                Integer qty = entry.getValue();
                if (qty == null || qty <= 0) continue;

                Topping topping = toppingRepository.findById(toppingId).orElse(null);
                if (topping == null) continue;

                SelectedTopping st = new SelectedTopping();
                st.setCartItem(item);
                st.setTopping(topping);
                st.setQuantity(qty * item.getQuantity());
                st.setUnitPrice(topping.getExtraPrice());
                st.setLineTotal(topping.getExtraPrice().multiply(BigDecimal.valueOf(st.getQuantity())));
                selectedToppingRepository.save(st);

                newToppingCost = newToppingCost.add(st.getLineTotal());
            }

            // Recompute line total
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                .add(newToppingCost);
            item.setLineTotal(lineTotal);
        }
    }

    public List<ProductVariant> listVariantsForProduct(Product product) {
        if (product == null) return List.of();
        return variantRepository.findByProduct(product);
    }
}
