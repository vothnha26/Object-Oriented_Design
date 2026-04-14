package com.alotra.entity.state;

import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderContextTest {

    @Test
    void advanceShouldMovePendingOrderToPreparing() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        OrderContext context = new OrderContext(order);
        context.advance();

        assertEquals(OrderStatus.PREPARING, order.getStatus());
        assertTrue(context.canCancel());
    }

    @Test
    void cancelShouldBeBlockedWhileDelivering() {
        Order order = new Order();
        order.setStatus(OrderStatus.DELIVERING);

        OrderContext context = new OrderContext(order);

        assertFalse(context.canCancel());
        assertThrows(IllegalStateException.class, context::cancel);
    }

    @Test
    void deliveredOrderShouldBeReviewable() {
        Order order = new Order();
        order.setStatus(OrderStatus.DELIVERED);

        OrderContext context = new OrderContext(order);

        assertTrue(context.canReview());
        assertThrows(IllegalStateException.class, context::advance);
    }
}
