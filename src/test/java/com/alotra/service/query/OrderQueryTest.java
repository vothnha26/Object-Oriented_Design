package com.alotra.service.query;

import com.alotra.dto.OrderDto;
import com.alotra.entity.Customer;
import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Query Pipeline: Template Method Unit Tests")
class OrderQueryTest {

    @Mock private OrderRepository orderRepository;
    
    private List<Order> testOrders;

    @BeforeEach
    void setUp() {
        testOrders = new ArrayList<>();
        
        Order o1 = new Order();
        o1.setId(1);
        o1.setStatus(OrderStatus.PENDING);
        o1.setCreatedAt(LocalDateTime.now().minusDays(2));
        Customer c1 = new Customer(); c1.setFullName("Nguyen Van A");
        o1.setCustomer(c1);
        testOrders.add(o1);

        Order o2 = new Order();
        o2.setId(2);
        o2.setStatus(OrderStatus.DELIVERED);
        o2.setCreatedAt(LocalDateTime.now().minusDays(1));
        Customer c2 = new Customer(); c2.setFullName("Tran Thi B");
        o2.setCustomer(c2);
        testOrders.add(o2);
    }

    @Test
    @DisplayName("StatusOrderFilter should match correct status")
    void testStatusOrderFilter() {
        StatusOrderFilter filter = new StatusOrderFilter(OrderStatus.PENDING);
        assertTrue(filter.matches(testOrders.get(0)));
        assertFalse(filter.matches(testOrders.get(1)));
    }

    @Test
    @DisplayName("KeywordFilter should match customer name")
    void testKeywordFilter() {
        KeywordFilter filter = new KeywordFilter("Nguyen");
        assertTrue(filter.matches(testOrders.get(0)));
        assertFalse(filter.matches(testOrders.get(1)));
    }

    @Test
    @DisplayName("AbstractOrderQuery should execute the workflow correctly")
    void testTemplateMethodExecution() {
        when(orderRepository.findAll()).thenReturn(testOrders);

        // Concrete implementation for testing (Anonymous class)
        AbstractOrderQuery query = new AbstractOrderQuery(orderRepository) {
            @Override
            protected OrderFilterStrategy getFilter() {
                return new StatusOrderFilter(OrderStatus.DELIVERED);
            }
        };

        // Act
        List<OrderDto> results = query.execute(null, 10);

        // Assert
        assertEquals(1, results.size());
        assertEquals(2, results.get(0).getId());
        assertEquals("DELIVERED", results.get(0).getStatus());
        
        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("Query should apply keyword and limit correctly")
    void testQueryWithKeywordAndLimit() {
        when(orderRepository.findAll()).thenReturn(testOrders);

        AbstractOrderQuery query = new AbstractOrderQuery(orderRepository) {
            @Override
            protected OrderFilterStrategy getFilter() {
                return o -> true; // Match all
            }
        };

        // Act & Assert: Keyword "Tran"
        List<OrderDto> resultsKw = query.execute("Tran", 10);
        assertEquals(1, resultsKw.size());
        assertEquals("Tran Thi B", resultsKw.get(0).getCustomerName());

        // Act & Assert: Limit 1
        List<OrderDto> resultsLimit = query.execute(null, 1);
        assertEquals(1, resultsLimit.size());
    }
}
