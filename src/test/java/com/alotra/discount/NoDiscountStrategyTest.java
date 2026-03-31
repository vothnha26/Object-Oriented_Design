package com.alotra.discount;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NoDiscountStrategy.
 */
public class NoDiscountStrategyTest {

    @Test
    public void testApplyReturnsOriginalPrice() {
        NoDiscountStrategy strategy = new NoDiscountStrategy();
        BigDecimal price = new BigDecimal("99.99");
        BigDecimal result = strategy.apply(price);
        
        // No discount should return original price
        assertEquals(price, result);
    }

    @Test
    public void testApplySeveralPrices() {
        NoDiscountStrategy strategy = new NoDiscountStrategy();
        
        BigDecimal[] prices = {
            BigDecimal.ZERO,
            new BigDecimal("1.50"),
            new BigDecimal("100.00"),
            new BigDecimal("999999.99")
        };
        
        for (BigDecimal price : prices) {
            assertEquals(price, strategy.apply(price));
        }
    }

    @Test
    public void testApplyNullPrice() {
        NoDiscountStrategy strategy = new NoDiscountStrategy();
        BigDecimal result = strategy.apply(null);
        
        // Null price should return null (consistent with other strategies)
        assertNull(result);
    }

    @Test
    public void testGetName() {
        NoDiscountStrategy strategy = new NoDiscountStrategy();
        String name = strategy.getName();
        
        assertNotNull(name);
        assertEquals("NoDiscount", name);
    }

    @Test
    public void testIsNullObject() {
        // Verify it acts as a Null Object pattern - safe to use anywhere
        NoDiscountStrategy strategy = new NoDiscountStrategy();
        assertNotNull(strategy);
        assertNotNull(strategy.getName());
        assertNotNull(strategy.apply(BigDecimal.TEN));
    }
}
