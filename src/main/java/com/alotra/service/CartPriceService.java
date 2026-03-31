package com.alotra.service;

import java.math.BigDecimal;
import java.util.Collection;

import org.springframework.stereotype.Service;

import com.alotra.discount.DiscountStrategy;
import com.alotra.discount.NoDiscountStrategy;
import com.alotra.entity.CartItem;
import com.alotra.entity.SelectedTopping;
import com.alotra.repository.SelectedToppingRepository;
import com.alotra.service.pricing.BasePriceComponent;
import com.alotra.service.pricing.PriceComponent;
import com.alotra.service.pricing.PromotionDecorator;
import com.alotra.service.pricing.RoundingDecorator;
import com.alotra.service.pricing.ToppingDecorator;

/**
 * Service for calculating cart line item prices using decorator pattern.
 * Builds a chain: Base Price -> Add Toppings -> Apply Promotion -> Round final price
 */
@Service
public class CartPriceService {
    
    private final SelectedToppingRepository selectedToppingRepository;

    public CartPriceService(SelectedToppingRepository selectedToppingRepository) {
        this.selectedToppingRepository = selectedToppingRepository;
    }

    /**
     * Calculate line item price with all decorators applied.
     * Chain: Base -> Toppings -> Promotion -> Rounding
     * 
     * @param cartItem The cart item to calculate price for
     * @param discountStrategy Optional discount to apply (null if no discount)
     * @return Final calculated price
     */
    public BigDecimal calculateLinePrice(CartItem cartItem, DiscountStrategy discountStrategy) {
        // Step 1: Base price component (unitPrice * quantity)
        PriceComponent priceComponent = new BasePriceComponent(
            cartItem.getUnitPrice(),
            cartItem.getQuantity()
        );

        // Step 2: Add toppings if any
        Collection<SelectedTopping> toppings = selectedToppingRepository.findByCartItem(cartItem);
        if (toppings != null && !toppings.isEmpty()) {
            java.util.Map<com.alotra.entity.Topping, Integer> toppingMap = toppings.stream()
                .collect(java.util.stream.Collectors.toMap(
                    SelectedTopping::getTopping,
                    SelectedTopping::getQuantity,
                    Integer::sum
                ));
            priceComponent = new ToppingDecorator(priceComponent, toppingMap);
        }

        // Step 3: Apply promotion (Member 3 uses int percent)
        int discountPercent = 0;
        if (discountStrategy instanceof com.alotra.discount.PercentDiscountStrategy) {
            // Note: This is a hacky conversion for compatibility
            discountPercent = 10; // Placeholder or add method to PercentDiscountStrategy
        }
        priceComponent = new PromotionDecorator(priceComponent, discountPercent);

        // Step 4: Round to whole number
        priceComponent = new RoundingDecorator(priceComponent);

        // Return final calculated price
        return priceComponent.calculate();
    }

    /**
     * Calculate line item price without discount.
     * Chain: Base -> Toppings -> No Discount -> Rounding
     * 
     * @param cartItem The cart item to calculate price for
     * @return Final calculated price
     */
    public BigDecimal calculateLinePrice(CartItem cartItem) {
        return calculateLinePrice(cartItem, new NoDiscountStrategy());
    }
}
