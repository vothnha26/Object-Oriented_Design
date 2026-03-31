package com.alotra.discount;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PercentDiscountStrategy.
 */
public class PercentDiscountStrategyTest {

    @Test
    public void testApplyPercentDiscount10Percent() {
        PercentDiscountStrategy strategy = new PercentDiscountStrategy(10);
        BigDecimal price = new BigDecimal("100.00");
        BigDecimal result = strategy.apply(price);
        
        // 100 * (100-10)/100 = 90
        assertEquals(new BigDecimal("90"), result);
    }

    @Test
    public void testApplyPercentDiscount50Percent() {
        PercentDiscountStrategy strategy = new PercentDiscountStrategy(50);
        BigDecimal price = new BigDecimal("200.00");
        BigDecimal result = strategy.apply(price);
        
        // 200 * (100-50)/100 = 100
        assertEquals(new BigDecimal("100"), result);
    }

    @Test
    public void testApplyPercentDiscount100Percent() {
        PercentDiscountStrategy strategy = new PercentDiscountStrategy(100);
        BigDecimal price = new BigDecimal("150.00");
        BigDecimal result = strategy.apply(price);
        
        // 100% discount = 0
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    public void testApplyZeroPercent() {
        PercentDiscountStrategy strategy = new PercentDiscountStrategy(0);
        BigDecimal price = new BigDecimal("100.00");
        BigDecimal result = strategy.apply(price);
        
        // 0% discount = original price
        assertEquals(price, result);
    }

    @Test
    public void testApplyPercentDiscountWithRounding() {
        PercentDiscountStrategy strategy = new PercentDiscountStrategy(33);
        BigDecimal price = new BigDecimal("100.00");
        BigDecimal result = strategy.apply(price);
        
        // 100 * (100-33)/100 = 67.0, should round to 67
        assertEquals(new BigDecimal("67"), result);
    }

    @Test
    public void testApplyNullPrice() {
        PercentDiscountStrategy strategy = new PercentDiscountStrategy(10);
        BigDecimal result = strategy.apply(null);
        
        // Null price should return null
        assertNull(result);
    }

    @Test
    public void testGetName() {
        PercentDiscountStrategy strategy = new PercentDiscountStrategy(20);
        assertTrue(strategy.getName().contains("20%"));
    }

    @Test
    public void testCapDiscount100Percent() {
        // Percent > 100 should be capped at 100
        PercentDiscountStrategy strategy = new PercentDiscountStrategy(150);
        BigDecimal price = new BigDecimal("100.00");
        BigDecimal result = strategy.apply(price);
        
        // Should cap at 100%, result = 0
        assertEquals(BigDecimal.ZERO, result);
    }
}
