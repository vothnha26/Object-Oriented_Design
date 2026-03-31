package com.alotra.merged;

import com.alotra.entity.*;
import com.alotra.entity.enums.*;
import com.alotra.repository.*;
import com.alotra.security.CustomerUserDetails;
import com.alotra.security.EmployeeUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("E2E Integrated Test Suite: Pattern Collaboration")
public class FullOrderE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ProductRepository productRepository;
    @MockitoBean private ProductVariantRepository variantRepository;
    @MockitoBean private CartRepository cartRepository;
    @MockitoBean private CartItemRepository cartItemRepository;
    @MockitoBean private OrderRepository orderRepository;

    private CustomerUserDetails customerDetails;
    private EmployeeUserDetails employeeDetails;

    @BeforeEach
    void setUp() {
        Customer c = new Customer();
        c.setId(1);
        c.setUsername("tai");
        customerDetails = new CustomerUserDetails(c);

        Employee e = new Employee();
        e.setId(99);
        e.setUsername("admin");
        e.setRole(EmployeeRole.ADMIN);
        employeeDetails = new EmployeeUserDetails(e);
    }

    @Test
    @DisplayName("E2E 1: Customer Order Flow (Builder Pattern)")
    void testCompleteOrderFlow() throws Exception {
        Product product = new Product(); product.setId(1);
        ProductVariant variant = new ProductVariant(); variant.setId(1); variant.setProduct(product);
        variant.setPrice(new BigDecimal("50000"));
        
        Cart cart = new Cart(); cart.setId(50);
        cart.setCustomer(customerDetails.getCustomer());
        
        CartItem item = new CartItem(); 
        item.setId(201); 
        item.setVariant(variant);
        item.setCart(cart); // Link item to cart
        item.setUnitPrice(new BigDecimal("50000"));
        item.setLineTotal(new BigDecimal("50000"));
        item.setQuantity(1);

        when(variantRepository.findById(1)).thenReturn(Optional.of(variant));
        when(cartRepository.findFirstByCustomerAndStatus(any(), eq(CartStatus.ACTIVE))).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllById(any())).thenReturn(List.of(item));
        when(orderRepository.save(any())).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(28); // Use ID from data_init.sql to satisfy DB constraints
            return o;
        });

        // Checkout process
        mockMvc.perform(post("/checkout/place").with(user(customerDetails))
                .param("itemIds", "201").param("paymentMethod", "CASH")
                .param("receivingMethod", "Pickup"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/orders"));
    }

    @Test
    @DisplayName("E2E 2: Admin Soft Delete & Undo (Command Pattern)")
    void testAdminDeleteAndUndo() throws Exception {
        Product p = new Product(); p.setId(500); p.setStatus(ProductStatus.ACTIVE);
        when(productRepository.findById(500)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/admin/products/delete/500").with(user(employeeDetails)))
                .andExpect(status().is3xxRedirection());
        
        mockMvc.perform(post("/admin/products/undo").with(user(employeeDetails)))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("E2E 3: Order History Query (Template Method)")
    void testOrderQuery() throws Exception {
        mockMvc.perform(get("/account/orders").with(user(customerDetails)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("items"));
    }

    @Test
    @DisplayName("E2E 4: Update Toppings in Cart (Command + Decorator)")
    void testUpdateToppings() throws Exception {
        Cart cart = new Cart(); cart.setId(50);
        cart.setCustomer(customerDetails.getCustomer());
        
        CartItem item = new CartItem(); 
        item.setId(201);
        item.setCart(cart); // Link item to cart
        
        when(cartRepository.findFirstByCustomerAndStatus(any(), any())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(201)).thenReturn(Optional.of(item));

        mockMvc.perform(post("/cart/item/201/toppings").with(user(customerDetails))
                .param("toppingQtys[1]", "2"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("E2E 5: Advance Order Status (State Pattern)")
    void testAdvanceOrderStatus() throws Exception {
        Order order = new Order();
        order.setId(28);
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(28)).thenReturn(Optional.of(order));

        mockMvc.perform(post("/vendor/orders/28/advance").with(user(employeeDetails)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vendor/orders"));
    }
}
