package com.alotra.service;

import com.alotra.entity.*;
import com.alotra.entity.enums.CartStatus;
import com.alotra.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService: Mock Tests with Pricing Decorator")
class CartServiceMockTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private ToppingRepository toppingRepository;
    @Mock private AppliedPromotionRepository appliedPromotionRepository;
    @Mock private SelectedToppingRepository selectedToppingRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderedToppingRepository orderedToppingRepository;

    @InjectMocks
    private CartService cartService;

    private Customer customer;
    private Cart cart;
    private ProductVariant variant;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1);

        cart = new Cart();
        cart.setId(100);
        cart.setCustomer(customer);
        cart.setStatus(CartStatus.ACTIVE);

        product = new Product();
        product.setId(200);

        variant = new ProductVariant();
        variant.setId(300);
        variant.setPrice(new BigDecimal("50000"));
        variant.setProduct(product);
    }

    @Test
    @DisplayName("Should successfully add item to cart with promotion and toppings")
    @SuppressWarnings("null")
    void testAddItemWithOptions() {
        // Arrange
        when(cartRepository.findFirstByCustomerAndStatus(customer, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(variantRepository.findById(300)).thenReturn(Optional.of(variant));
        
        // Mock 10% discount
        when(appliedPromotionRepository.findActiveMaxDiscountPercentForProduct(200))
                .thenReturn(10);

        Topping topping = new Topping();
        topping.setId(400);
        topping.setName("Pearl");
        topping.setExtraPrice(new BigDecimal("5000"));
        when(toppingRepository.findById(400)).thenReturn(Optional.of(topping));

        Map<Integer, Integer> toppingQty = new HashMap<>();
        toppingQty.put(400, 1);

        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            item.setId(500);
            return item;
        });

        // Act
        // Base 50k - 10% (5k) = 45k. Add topping 5k = 50k. Qty 2 = 100k.
        CartItem result = cartService.addItemWithOptions(customer, 300, 2, toppingQty, "No sugar");

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("45000"), result.getUnitPrice()); // Unit price after promo, before toppings
        assertEquals(new BigDecimal("100000"), result.getLineTotal()); // (45k + 5k) * 2
        assertEquals(2, result.getQuantity());
        
        verify(appliedPromotionRepository).findActiveMaxDiscountPercentForProduct(200);
        verify(cartItemRepository).save(any(CartItem.class));
        verify(selectedToppingRepository).save(any(SelectedTopping.class));
    }
}
