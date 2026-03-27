package com.alotra.service.proxy;

import com.alotra.entity.Employee;
import com.alotra.entity.Order;
import com.alotra.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipperOrderOperationsProxyTest {

    @Mock
    private ShipperOrderOperations real;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ShipperOrderOperationsProxy proxy;

    @Test
    void markAsDelivered_authorized_shouldDelegate() {
        Order order = order(1, 50);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(real.markAsDelivered(1, 50)).thenReturn(true);

        boolean ok = proxy.markAsDelivered(1, 50);

        assertTrue(ok);
        verify(real).markAsDelivered(1, 50);
    }

    @Test
    void markAsDelivered_unauthorized_shouldReturnFalseAndNotDelegate() {
        Order order = order(1, 99);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        boolean ok = proxy.markAsDelivered(1, 50);

        assertFalse(ok);
        verify(real, never()).markAsDelivered(1, 50);
    }

    @Test
    void confirmPayment_authorized_shouldDelegate() {
        Order order = order(9, 7);
        when(orderRepository.findById(9)).thenReturn(Optional.of(order));
        when(real.confirmPayment(9, 7)).thenReturn(true);

        boolean ok = proxy.confirmPayment(9, 7);

        assertTrue(ok);
        verify(real).confirmPayment(9, 7);
    }

    @Test
    void acceptOrder_shouldPassThroughWithoutOwnershipCheck() {
        when(real.acceptOrder(100, 12)).thenReturn(true);

        boolean ok = proxy.acceptOrder(100, 12);

        assertTrue(ok);
        verify(real).acceptOrder(100, 12);
        verify(orderRepository, never()).findById(100);
    }

    private Order order(int orderId, int shipperId) {
        Employee owner = new Employee();
        owner.setId(shipperId);
        Order order = new Order();
        order.setId(orderId);
        order.setEmployee(owner);
        return order;
    }
}