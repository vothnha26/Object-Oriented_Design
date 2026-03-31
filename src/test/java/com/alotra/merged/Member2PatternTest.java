package com.alotra.merged;

import com.alotra.discount.*;
import com.alotra.payment.*;
import com.alotra.entity.enums.PaymentMethod;
import com.alotra.service.*;
import com.alotra.entity.Cart;
import com.alotra.entity.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class Member2PatternTest {

    @Test
    @DisplayName("Test Strategy: PaymentStrategyFactory should return correct strategy")
    void testPaymentFactory() {
        PaymentStrategyFactory factory = new PaymentStrategyFactory();
        
        PaymentStrategy cash = factory.getStrategy(PaymentMethod.CASH);
        assertTrue(cash instanceof CashPaymentStrategy);
        assertEquals("Cash on Delivery", cash.getMethodName());

        PaymentStrategy bank = factory.getStrategy(PaymentMethod.BANK_TRANSFER);
        assertTrue(bank instanceof BankTransferPaymentStrategy);
        assertEquals("Bank Transfer", bank.getMethodName());
    }

    @Test
    @DisplayName("Test Strategy: DiscountStrategy implementations")
    void testDiscountStrategies() {
        BigDecimal price = new BigDecimal("100000");

        DiscountStrategy noDiscount = new NoDiscountStrategy();
        assertTrue(new BigDecimal("100000").compareTo(noDiscount.apply(price)) == 0);

        DiscountStrategy percentDiscount = new PercentDiscountStrategy(10); // 10%
        assertTrue(new BigDecimal("90000").compareTo(percentDiscount.apply(price)) == 0);
    }

    @Test
    @DisplayName("Test Facade: CartFacade delegation")
    void testCartFacade() {
        CartManagementService mgmt = mock(CartManagementService.class);
        CartItemService itemService = mock(CartItemService.class);
        CartFacade facade = new CartFacade(mgmt, itemService);

        Customer customer = new Customer();
        
        facade.getOrCreateActiveCart(customer);
        verify(mgmt, times(1)).getOrCreateActiveCart(customer);

        facade.listItems(customer);
        verify(itemService, times(1)).listItems(customer);
    }
}
