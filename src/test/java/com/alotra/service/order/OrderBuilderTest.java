package com.alotra.service.order;

import com.alotra.entity.Customer;
import com.alotra.entity.Order;
import com.alotra.entity.OrderItem;
import com.alotra.entity.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class OrderBuilderTest {

    @Test
    void buildShouldPopulateOrderAndBackReferenceItems() {
        Customer customer = new Customer();
        OrderItem item = new OrderItem();
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(30000));

        Order order = OrderBuilder.builder()
                .forCustomer(customer)
                .shipTo("123 Test Street")
                .withItems(List.of(item))
                .build();

        assertSame(customer, order.getCustomer());
        assertEquals("123 Test Street", order.getShippingAddressLine());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(1, order.getItems().size());
        assertSame(order, order.getItems().get(0).getOrder());
    }
}
