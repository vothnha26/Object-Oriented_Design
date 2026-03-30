package com.alotra.command;

import com.alotra.service.CartService;
import com.alotra.entity.Customer;
import java.util.Map;
import java.util.HashMap;

public class UpdateToppingsCommand implements CartCommand {
    private final CartService cartService;
    private final Customer customer;
    private final Integer itemId;
    private final Map<Integer, Integer> newToppingQty;
    private Map<Integer, Integer> oldToppingQty; // Snapshot for Undo
    private final String description;

    public UpdateToppingsCommand(CartService cartService, Customer customer, Integer itemId, Map<Integer, Integer> newToppingQty, String description) {
        this.cartService = cartService;
        this.customer = customer;
        this.itemId = itemId;
        this.newToppingQty = newToppingQty;
        this.description = description;
    }

    @Override
    public void execute() {
        // 1. Take snapshot before change
        this.oldToppingQty = cartService.getCurrentToppingQtys(itemId);
        // 2. Perform update
        cartService.updateToppings(customer, itemId, newToppingQty);
    }

    @Override
    public void undo() {
        if (oldToppingQty != null) {
            cartService.updateToppings(customer, itemId, oldToppingQty);
        }
    }

    @Override
    public String getDescription() {
        return description;
    }
}
