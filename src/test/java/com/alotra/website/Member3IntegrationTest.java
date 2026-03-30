package com.alotra.website;

import com.alotra.entity.*;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.enums.ProductStatus;
import com.alotra.entity.enums.ToppingStatus;
import com.alotra.repository.*;
import com.alotra.service.CartService;
import com.alotra.service.VendorOrderService;
import com.alotra.dto.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@DisplayName("Member 3: Real DB Integration Tests (With Rollback)")
class Member3IntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private VendorOrderService vendorOrderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSizeRepository productSizeRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ToppingRepository toppingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager em;

    private Customer testCustomer;
    private ProductVariant testVariant;
    private Topping testTopping;

    @BeforeEach
    void setUp() {
        // 1. Create Category
        Category cat = new Category();
        cat.setName("Integration Test Category " + System.currentTimeMillis());
        cat = categoryRepository.save(cat);

        // 2. Create Product
        Product p = new Product();
        p.setName("Integration Matcha");
        p.setCategory(cat);
        p.setStatus(ProductStatus.ACTIVE);
        p = productRepository.save(p);

        // 3. Create ProductSize
        ProductSize size = new ProductSize();
        size.setName("Size L");
        size.setPriceAdjustment(BigDecimal.ZERO);
        size.setStatus(ProductStatus.ACTIVE);
        size = productSizeRepository.save(size);

        // 4. Create Variant
        testVariant = new ProductVariant();
        testVariant.setProduct(p);
        testVariant.setSize(size);
        testVariant.setPrice(new BigDecimal("50000"));
        testVariant.setStatus(ProductStatus.ACTIVE);
        testVariant = variantRepository.save(testVariant);

        // 5. Create Topping
        testTopping = new Topping();
        testTopping.setName("Pearl " + System.currentTimeMillis());
        testTopping.setExtraPrice(new BigDecimal("5000"));
        testTopping.setStatus(ToppingStatus.AVAILABLE);
        testTopping = toppingRepository.save(testTopping);

        // 6. Create Customer
        testCustomer = new Customer();
        testCustomer.setFullName("Test User");
        testCustomer.setUsername("testuser_" + System.currentTimeMillis());
        testCustomer.setPasswordHash("hashed_password");
        testCustomer.setEmail("test_" + System.currentTimeMillis() + "@example.com");
        testCustomer = customerRepository.save(testCustomer);
    }

    @Test
    @DisplayName("Task 1: Pricing Decorator - Add to cart with real DB persistence")
    void testPricingDecoratorIntegration() {
        Map<Integer, Integer> toppings = new HashMap<>();
        toppings.put(testTopping.getId(), 2); // 2 units of pearls

        // Act: Add 2 units of Matcha with 2 pearls each
        CartItem item = cartService.addItemWithOptions(testCustomer, testVariant.getId(), 2, toppings, "Less Ice");

        // Assert: Check persistence
        assertNotNull(item.getId());
        List<CartItem> dbItems = cartItemRepository.findByCart(item.getCart());
        assertFalse(dbItems.isEmpty());

        // Price calculation verification:
        // Base: 50,000
        // Topping: 5,000 * 2 = 10,000
        // Total per unit: 60,000
        // Quantity: 2
        // Line Total: 120,000
        assertEquals(0, new BigDecimal("120000.00").compareTo(item.getLineTotal()));

        System.out.println("Integration Check: CartItem ID " + item.getId() + " total: " + item.getLineTotal());
    }

    @Test
    @DisplayName("Task 2: Admin Command - Order status transition in real DB")
    void testAdminCommandIntegration() {
        // 1. Manually create an order
        Order order = new Order();
        order.setCustomer(testCustomer);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("100000"));
        order.setSubtotal(new BigDecimal("100000"));
        order.setDiscount(BigDecimal.ZERO);
        order.setShippingFee(BigDecimal.ZERO);
        order = orderRepository.save(order);

        // 2. Act: Advance status via Service (uses Command Pattern internally)
        vendorOrderService.updateStatus(order.getId(), OrderStatus.PREPARING);

        // Sync JPA with DB to see direct JDBC updates
        em.flush();
        em.clear();

        // 3. Assert: Verify via repository and JDBC
        Order updated = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.PREPARING, updated.getStatus());

        String statusInDb = jdbc.queryForObject("SELECT TrangThaiDonHang FROM Orders WHERE MaDH = ?", String.class,
                order.getId());
        assertEquals("PREPARING", statusInDb);
    }

    @Test
    @DisplayName("Task 3: Order Query Pipeline - Keyword filtering on real DB")
    void testOrderQueryPipelineIntegration() {
        // 1. Create orders
        Order o1 = new Order();
        o1.setCustomer(testCustomer);
        o1.setStatus(OrderStatus.DELIVERED);
        o1.setTotalAmount(new BigDecimal("50000"));
        o1.setSubtotal(new BigDecimal("50000"));
        o1.setDiscount(BigDecimal.ZERO);
        o1.setShippingFee(BigDecimal.ZERO);
        orderRepository.save(o1);

        // 2. Act: Query via Service (uses Template Method Pattern internally)
        List<OrderDto> results = vendorOrderService.listOrders("DELIVERED", null, 10);

        // 3. Assert
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> r.getId().equals(o1.getId())));
    }
}
