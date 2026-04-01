package com.alotra.website;

import com.alotra.entity.*;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.enums.ProductStatus;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import com.alotra.service.CustomerService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Member 3: End-to-End Integration Tests (MockMvc)")
class Member3E2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductRepository productRepository;
    @MockitoBean
    private ProductVariantRepository variantRepository;
    @MockitoBean
    private CartRepository cartRepository;
    @MockitoBean
    private OrderRepository orderRepository;
    @MockitoBean
    private CustomerRepository customerRepository;
    @MockitoBean
    private ToppingRepository toppingRepository;
    @MockitoBean
    private SelectedToppingRepository selectedToppingRepository;
    @MockitoBean
    private CartItemRepository cartItemRepository;
    @MockitoBean
    private AppliedPromotionRepository appliedPromotionRepository;
    @MockitoBean
    private JdbcTemplate jdbc;
    @MockitoBean
    private CustomerService customerService;

    private CustomerUserDetails customerDetails;
    private EmployeeUserDetails employeeDetails;

    @BeforeEach
    void setUp() {
        Customer c = new Customer();
        c.setId(1);
        c.setUsername("user1");
        customerDetails = new CustomerUserDetails(c);

        Employee e = new Employee();
        e.setId(99);
        e.setUsername("admin1");
        employeeDetails = new EmployeeUserDetails(e);
    }

    @Test
    @DisplayName("E2E: Add product with options to cart and verify redirect")
    void testAddToCartFlow() throws Exception {
        Product p = new Product();
        p.setId(10);
        p.setName("Matcha");

        ProductVariant v = new ProductVariant();
        v.setId(20);
        v.setPrice(new BigDecimal("40000"));
        v.setProduct(p);
        v.setStatus(ProductStatus.ACTIVE);

        when(productRepository.findById(10)).thenReturn(Optional.of(p));
        when(variantRepository.findById(20)).thenReturn(Optional.of(v));
        when(customerService.findByUsername(any())).thenReturn(customerDetails.getCustomer());
        // Mock save() to return the input item
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.findFirstByCustomerAndStatus(any(), any())).thenReturn(Optional.of(new Cart()));

        mockMvc.perform(post("/products/10/add-to-cart")
                .with(user(customerDetails))
                .param("variantId", "20")
                .param("qty", "1")
                .param("sugar", "70%")
                .param("ice", "50%"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("E2E: Admin update order status and verify command execution")
    void testAdminUpdateStatusFlow() throws Exception {
        Order o = new Order();
        o.setId(500);
        o.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(500)).thenReturn(Optional.of(o));
        // Mock JdbcTemplate calls inside UpdateOrderStatusCommand
        when(jdbc.queryForObject(anyString(), eq(String.class), eq(500))).thenReturn("PENDING");

        mockMvc.perform(post("/vendor/orders/500/advance")
                .with(user(employeeDetails))
                .param("from", "list"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vendor/orders"));
        
        // Success advance should move status PENDING -> PREPARING
        verify(jdbc).update(anyString(), eq("PREPARING"), eq(500));
    }

    @Test
    @DisplayName("E2E: List orders with keyword filtering")
    void testListOrdersQueryFlow() throws Exception {
        Order o = new Order();
        o.setId(1);
        o.setCreatedAt(LocalDateTime.now());
        o.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findAll()).thenReturn(Collections.singletonList(o));

        mockMvc.perform(get("/vendor/orders")
                .with(user(employeeDetails))
                .param("kw", "Nguyen"))
                .andExpect(status().isOk())
                .andExpect(view().name("vendor/orders"))
                .andExpect(model().attributeExists("items"));
    }
}
