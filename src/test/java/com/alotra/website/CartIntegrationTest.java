package com.alotra.website;

import com.alotra.command.CartHistoryManager;
import com.alotra.entity.*;
import com.alotra.entity.enums.ProductStatus;
import com.alotra.entity.enums.ToppingStatus;
import com.alotra.repository.*;
import com.alotra.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CartIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartHistoryManager historyManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductSizeRepository productSizeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ToppingRepository toppingRepository;

    private Customer testUser;
    private ProductVariant variantM;
    private ProductVariant variantL;
    private Topping topping1;

    @BeforeEach
    void setup() {
        String suffix = String.valueOf(System.currentTimeMillis());
        testUser = new Customer();
        testUser.setUsername("user" + suffix);
        testUser.setPasswordHash("{noop}password");
        testUser.setEmail("test" + suffix + "@test.com");
        testUser.setFullName("Integration Tester");
        testUser = customerRepository.save(testUser);

        Category cat = new Category();
        cat.setName("Cat" + suffix);
        cat = categoryRepository.save(cat);

        Product product = new Product();
        product.setName("Tea" + suffix);
        product.setCategory(cat);
        product.setStatus(ProductStatus.ACTIVE);
        product = productRepository.save(product);

        ProductSize sizeM = new ProductSize();
        sizeM.setName("M" + suffix);
        sizeM = productSizeRepository.save(sizeM);

        ProductSize sizeL = new ProductSize();
        sizeL.setName("L" + suffix);
        sizeL = productSizeRepository.save(sizeL);

        variantM = new ProductVariant();
        variantM.setProduct(product);
        variantM.setSize(sizeM);
        variantM.setPrice(new BigDecimal("40000"));
        variantM.setStatus(ProductStatus.ACTIVE);
        variantM = variantRepository.save(variantM);

        variantL = new ProductVariant();
        variantL.setProduct(product);
        variantL.setSize(sizeL);
        variantL.setPrice(new BigDecimal("50000"));
        variantL.setStatus(ProductStatus.ACTIVE);
        variantL = variantRepository.save(variantL);

        topping1 = new Topping();
        topping1.setName("Pearl" + suffix);
        topping1.setExtraPrice(new BigDecimal("5000"));
        topping1.setStatus(ToppingStatus.AVAILABLE);
        topping1 = toppingRepository.save(topping1);
        
        historyManager.clearHistory();
    }

    @Test
    @DisplayName("Logic #1: High Quantity Multiplier")
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void testHighQuantityMultiplier() {
        // Add 10 items of variantM (40k each) -> 400,000
        CartItem item = cartService.addItemWithOptions(testUser, variantM.getId(), 10, Map.of(), "");
        assertEquals(0, new BigDecimal("400000").compareTo(item.getLineTotal()), "Total for 10 items should be 400,000");
    }

    @Test
    @DisplayName("Logic #2: Decorator - Size Swap preserves toppings")
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void testSizeSwapDecorator() {
        // 1. Add variantM (40k) + Topping (5k) = 45k
        CartItem item = cartService.addItemWithOptions(testUser, variantM.getId(), 1, Map.of(), "");
        cartService.updateToppings(testUser, item.getId(), Map.of(topping1.getId(), 1));
        
        CartItem updated = cartService.listItems(testUser).get(0);
        assertEquals(0, new BigDecimal("45000").compareTo(updated.getLineTotal()));

        // 2. Change to variantL (50k). Expected: 50k + 5k = 55k
        cartService.changeVariant(testUser, updated.getId(), variantL.getId());
        
        CartItem swapped = cartService.listItems(testUser).get(0);
        assertEquals(0, new BigDecimal("55000").compareTo(swapped.getLineTotal()), "Price after size swap to L should be 55,000");
    }

    @Test
    @DisplayName("Logic #3: Command Pattern - Undo Topping Update")
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void testUndoToppingUpdate() throws Exception {
        // 1. Add item (40k)
        CartItem item = cartService.addItemWithOptions(testUser, variantM.getId(), 1, Map.of(), "");
        
        // 2. Update topping via Command (simulating user action)
        com.alotra.command.UpdateToppingsCommand cmd = new com.alotra.command.UpdateToppingsCommand(
            cartService, testUser, item.getId(), Map.of(topping1.getId(), 1), "Cập nhật topping"
        );
        historyManager.executeCommand(cmd);

        CartItem withTopping = cartService.listItems(testUser).get(0);
        assertEquals(0, new BigDecimal("45000").compareTo(withTopping.getLineTotal()), "Should be 45k after adding topping");

        // 3. Perform Undo
        historyManager.undo();

        CartItem afterUndo = cartService.listItems(testUser).get(0);
        assertEquals(0, new BigDecimal("40000").compareTo(afterUndo.getLineTotal()), "Total should return to 40,000 after undo");
    }
}
