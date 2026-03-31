package com.alotra.payment;

import com.alotra.entity.Order;
import com.alotra.entity.Payment;
import com.alotra.entity.enums.PaymentMethod;
import com.alotra.entity.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaymentStrategy implementations.
 */
public class PaymentStrategyTest {

    private Order order;
    private Payment payment;

    @BeforeEach
    public void setUp() {
        order = new Order();
        payment = new Payment();
        order.setPayment(payment);
    }

    @Test
    public void testCashPaymentStrategyAlwaysValid() {
        CashPaymentStrategy strategy = new CashPaymentStrategy();
        payment.setStatus(PaymentStatus.UNPAID);
        
        // COD should be valid even with UNPAID payment
        assertTrue(strategy.validatePayment(order));
    }

    @Test
    public void testCashPaymentStrategyNotRequirePrePayment() {
        CashPaymentStrategy strategy = new CashPaymentStrategy();
        
        assertFalse(strategy.requiresPrePayment());
    }

    @Test
    public void testCashPaymentStrategyGetMethodName() {
        CashPaymentStrategy strategy = new CashPaymentStrategy();
        
        assertEquals("Cash on Delivery", strategy.getMethodName());
    }

    @Test
    public void testCashPaymentStrategyNullOrder() {
        CashPaymentStrategy strategy = new CashPaymentStrategy();
        
        assertFalse(strategy.validatePayment(null));
    }

    @Test
    public void testCashPaymentStrategyNullPayment() {
        CashPaymentStrategy strategy = new CashPaymentStrategy();
        order.setPayment(null);
        
        assertFalse(strategy.validatePayment(order));
    }

    @Test
    public void testBankTransferPaymentStrategyRequprePrePayment() {
        BankTransferPaymentStrategy strategy = new BankTransferPaymentStrategy();
        
        assertTrue(strategy.requiresPrePayment());
    }

    @Test
    public void testBankTransferPaymentStrategyValidWhenPaid() {
        BankTransferPaymentStrategy strategy = new BankTransferPaymentStrategy();
        payment.setStatus(PaymentStatus.PAID);
        
        assertTrue(strategy.validatePayment(order));
    }

    @Test
    public void testBankTransferPaymentStrategyInvalidWhenUnpaid() {
        BankTransferPaymentStrategy strategy = new BankTransferPaymentStrategy();
        payment.setStatus(PaymentStatus.UNPAID);
        
        assertFalse(strategy.validatePayment(order));
    }

    @Test
    public void testBankTransferPaymentStrategyInvalidWhenOther() {
        BankTransferPaymentStrategy strategy = new BankTransferPaymentStrategy();
        payment.setStatus(PaymentStatus.UNPAID);
        
        assertFalse(strategy.validatePayment(order));
    }

    @Test
    public void testBankTransferPaymentStrategyNullOrder() {
        BankTransferPaymentStrategy strategy = new BankTransferPaymentStrategy();
        
        assertFalse(strategy.validatePayment(null));
    }

    @Test
    public void testBankTransferPaymentStrategyNullPayment() {
        BankTransferPaymentStrategy strategy = new BankTransferPaymentStrategy();
        order.setPayment(null);
        
        assertFalse(strategy.validatePayment(order));
    }

    @Test
    public void testBankTransferPaymentStrategyGetMethodName() {
        BankTransferPaymentStrategy strategy = new BankTransferPaymentStrategy();
        
        assertEquals("Bank Transfer", strategy.getMethodName());
    }

    @Test
    public void testPaymentStrategyFactoryGetCashStrategy() {
        PaymentStrategyFactory factory = new PaymentStrategyFactory();
        PaymentStrategy strategy = factory.getStrategy(PaymentMethod.CASH);
        
        assertNotNull(strategy);
        assertFalse(strategy.requiresPrePayment());
    }

    @Test
    public void testPaymentStrategyFactoryGetBankTransferStrategy() {
        PaymentStrategyFactory factory = new PaymentStrategyFactory();
        PaymentStrategy strategy = factory.getStrategy(PaymentMethod.BANK_TRANSFER);
        
        assertNotNull(strategy);
        assertTrue(strategy.requiresPrePayment());
    }

    @Test
    public void testPaymentStrategyFactoryNullMethod() {
        PaymentStrategyFactory factory = new PaymentStrategyFactory();
        
        assertThrows(IllegalArgumentException.class, () -> factory.getStrategy((PaymentMethod) null));
    }

    @Test
    public void testPaymentStrategyFactoryByString() {
        PaymentStrategyFactory factory = new PaymentStrategyFactory();
        PaymentStrategy strategy = factory.getStrategy("CASH");
        
        assertNotNull(strategy);
        assertEquals("Cash on Delivery", strategy.getMethodName());
    }
}
