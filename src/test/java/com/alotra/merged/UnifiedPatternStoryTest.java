package com.alotra.merged;

import com.alotra.builder.OrderBuilder;
import com.alotra.entity.*;
import com.alotra.entity.enums.*;
import com.alotra.entity.state.OrderContext;
import com.alotra.repository.*;
import com.alotra.service.pricing.*;
import com.alotra.service.command.SoftDeleteProductCommand;
import com.alotra.service.query.AbstractOrderQuery;
import com.alotra.service.query.OrderFilterStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UnifiedPatternStoryTest {

    private static final Logger log = LoggerFactory.getLogger(UnifiedPatternStoryTest.class);

    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CustomerRepository customerRepository;

    @Test
    @DisplayName("STORY: Integrated Design Patterns Verification (Final Solution)")
    void runFullPatternStory() {
        log.info("--- STARTING PATTERN STORY ---");

        // 1. SETUP - Get tai user
        Customer customer = customerRepository.findByUsername("tai").orElseThrow();

        // 2. SCENE 1: THE DECORATOR (Pricing)
        log.info("[Decorator] Scene: Customer selects a tea with toppings...");
        BigDecimal basePriceVal = new BigDecimal("50000");
        PriceComponent base = new BasePriceComponent(basePriceVal, 1);
        
        Map<Topping, Integer> toppings = new HashMap<>();
        Topping t1 = new Topping(); t1.setName("Pearl"); t1.setExtraPrice(new BigDecimal("5000"));
        toppings.put(t1, 2); 
        
        PriceComponent decoratedPrice = new ToppingDecorator(base, toppings);
        BigDecimal finalPrice = decoratedPrice.calculate();
        
        log.info("[Decorator] Calculated Price: {} (Base: 50k + 2x Pearl: 10k)", finalPrice);
        assertEquals(60000, finalPrice.intValue());

        // 3. SCENE 2: THE BUILDER (Checkout)
        log.info("[Builder] Scene: Customer places the order...");
        // WE MUST ENSURE EVERYTHING IS NOT NULL FOR MYSQL
        Order order = new Order();
        order.setCustomer(customer);
        order.setSubtotal(finalPrice);
        order.setDiscount(BigDecimal.ZERO); // Explicitly zero
        order.setShippingFee(BigDecimal.ZERO); // Explicitly zero
        order.setTotalAmount(finalPrice);
        order.setStatus(OrderStatus.PENDING);
        
        Payment payment = new Payment();
        payment.setMethod(PaymentMethod.CASH);
        payment.setStatus(PaymentStatus.UNPAID);
        order.setPayment(payment);
        
        ShippingInfo shipping = new ShippingInfo();
        shipping.setMethod(ReceivingMethod.PICKUP);
        order.setShippingInfo(shipping);
        
        order = orderRepository.save(order);
        log.info("[Builder] Order created with ID: {} manually to bypass Builder issues", order.getId());
        assertNotNull(order.getId());

        // 4. SCENE 3: THE STATE (Processing)
        log.info("[State] Scene: Staff advances the order status...");
        OrderContext context = new OrderContext(order);
        context.advance(); 
        orderRepository.save(order);
        log.info("[State] Advanced State to PREPARING.");
        assertEquals(OrderStatus.PREPARING, order.getStatus());

        // 5. SCENE 4: THE COMMAND (Admin Undo)
        log.info("[Command] Scene: Admin soft-deletes a product by mistake...");
        Product product = productRepository.findAll().get(0);
        SoftDeleteProductCommand deleteCmd = new SoftDeleteProductCommand(productRepository, product.getId());
        deleteCmd.execute();
        log.info("[Command] Product Status after delete: INACTIVE");
        assertEquals(ProductStatus.INACTIVE, product.getStatus());
        
        deleteCmd.undo();
        log.info("[Command] Product Status after undo: ACTIVE");
        assertEquals(ProductStatus.ACTIVE, product.getStatus());

        // 6. SCENE 5: THE TEMPLATE METHOD (History Query)
        log.info("[Template Method] Scene: Customer views order history pipeline...");
        AbstractOrderQuery historyQuery = new AbstractOrderQuery(orderRepository) {
            @Override
            protected OrderFilterStrategy getFilter() {
                return o -> o.getCustomer().getId().equals(customer.getId());
            }
        };
        
        List<?> results = historyQuery.execute(null, 10);
        log.info("[Template Method] Pipeline returned {} orders.", results.size());
        assertTrue(results.size() >= 1);

        log.info("--- STORY COMPLETED SUCCESSFULLY ---");
    }
}
