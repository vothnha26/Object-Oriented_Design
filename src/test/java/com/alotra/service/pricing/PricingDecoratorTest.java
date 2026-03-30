package com.alotra.service.pricing;

import com.alotra.entity.Topping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Calculation Engine: Pricing Decorator Unit Tests")
class PricingDecoratorTest {

    @Test
    @DisplayName("Should return base price correctly")
    void testBasePrice() {
        PriceComponent base = new BasePrice(new BigDecimal("50000"));
        assertEquals(new BigDecimal("50000"), base.calculate());
    }

    @Test
    @DisplayName("Should apply promotion discount correctly")
    void testPromotionDecorator() {
        PriceComponent base = new BasePrice(new BigDecimal("100000"));
        // 20% discount of 100,000 should be 80,000
        PriceComponent promo = new PromotionDecorator(base, 20);
        assertEquals(new BigDecimal("80000"), promo.calculate());
    }

    @Test
    @DisplayName("Should add topping prices correctly")
    void testToppingDecorator() {
        PriceComponent base = new BasePrice(new BigDecimal("40000"));
        
        Topping topping1 = mock(Topping.class);
        when(topping1.getExtraPrice()).thenReturn(new BigDecimal("5000"));
        Topping topping2 = mock(Topping.class);
        when(topping2.getExtraPrice()).thenReturn(new BigDecimal("10000"));

        Map<Topping, Integer> toppings = new HashMap<>();
        toppings.put(topping1, 2); // 10,000
        toppings.put(topping2, 1); // 10,000
        
        // 40,000 + 10,000 + 10,000 = 60,000
        PriceComponent toppingDec = new ToppingDecorator(base, toppings);
        assertEquals(new BigDecimal("60000"), toppingDec.calculate());
    }

    @Test
    @DisplayName("Should multiply by quantity correctly")
    void testQuantityDecorator() {
        PriceComponent base = new BasePrice(new BigDecimal("30000"));
        // 30,000 * 3 = 90,000
        PriceComponent qty = new QuantityDecorator(base, 3);
        assertEquals(new BigDecimal("90000"), qty.calculate());
    }

    @Test
    @DisplayName("Should handle full chain: ((Base - Promo) + Toppings) * Qty")
    void testFullChain() {
        // Base: 100,000
        PriceComponent chain = new BasePrice(new BigDecimal("100000"));
        
        // Promo 10%: 100,000 -> 90,000
        chain = new PromotionDecorator(chain, 10);
        
        // Topping: + 5,000
        Topping topping = mock(Topping.class);
        when(topping.getExtraPrice()).thenReturn(new BigDecimal("5000"));
        Map<Topping, Integer> toppings = new HashMap<>();
        toppings.put(topping, 1);
        chain = new ToppingDecorator(chain, toppings); // 90,000 + 5,000 = 95,000
        
        // Qty: 2 -> 95,000 * 2 = 190,000
        chain = new QuantityDecorator(chain, 2);
        
        assertEquals(new BigDecimal("190000"), chain.calculate());
    }
}
