package com.alotra.service.proxy;

import com.alotra.entity.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CartOperations {
    Cart getOrCreateActiveCart(Customer customer);

    CartItem addItemWithOptions(Customer customer, Integer variantId, int qty, Map<Integer, Integer> toppingQty, String note);

    List<CartItem> listItems(Customer customer);

    int getItemCount(Customer customer);

    void updateQuantity(Customer customer, Integer itemId, int qty);

    void removeItem(Customer customer, Integer itemId);

    BigDecimal calcTotal(List<CartItem> items);

    Order checkoutWithOptions(Customer customer, List<Integer> itemIds, String paymentMethod,
                              String note, String receivingMethod,
                              String shipName, String shipPhone, String shipAddress);

    List<Topping> listActiveToppings();

    Map<Integer, List<SelectedTopping>> getToppingsForItems(List<CartItem> items);

    void updateToppings(Customer customer, Integer itemId, Map<Integer, Integer> toppingQtyById);

    void changeVariant(Customer customer, Integer itemId, Integer newVariantId);

    List<ProductVariant> listVariantsForProduct(Product product);
}