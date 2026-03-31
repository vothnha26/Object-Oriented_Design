package com.alotra.service.proxy;

import com.alotra.entity.Cart;
import com.alotra.entity.CartItem;
import com.alotra.entity.Customer;
import com.alotra.entity.Order;
import com.alotra.repository.CartItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartOperationsProxyTest {

    @Mock
    private CartOperations real;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartOperationsProxy proxy;

    @Test
    void updateQuantity_authorized_shouldDelegate() {
        Customer actor = customer(1);
        CartItem item = cartItem(10, 1);
        when(cartItemRepository.findById(10)).thenReturn(Optional.of(item));

        proxy.updateQuantity(actor, 10, 3);

        verify(real).updateQuantity(actor, 10, 3);
    }

    @Test
    void updateQuantity_unauthorized_shouldThrowAndNotDelegate() {
        Customer actor = customer(1);
        CartItem item = cartItem(10, 2);
        when(cartItemRepository.findById(10)).thenReturn(Optional.of(item));

        assertThrows(SecurityException.class, () -> proxy.updateQuantity(actor, 10, 3));
        verify(real, never()).updateQuantity(actor, 10, 3);
    }

    @Test
    void removeItem_unauthorized_shouldThrowAndNotDelegate() {
        Customer actor = customer(1);
        CartItem item = cartItem(99, 5);
        when(cartItemRepository.findById(99)).thenReturn(Optional.of(item));

        assertThrows(SecurityException.class, () -> proxy.removeItem(actor, 99));
        verify(real, never()).removeItem(actor, 99);
    }

    @Test
    void checkoutWithOptions_shouldPassThrough() {
        Customer actor = customer(1);
        Order expected = new Order();
        expected.setId(123);
        when(real.checkoutWithOptions(actor, List.of(1, 2), "CASH", "note", "Ship", "n", "p", "a"))
                .thenReturn(expected);

        Order actual = proxy.checkoutWithOptions(actor, List.of(1, 2), "CASH", "note", "Ship", "n", "p", "a");

        assertEquals(123, actual.getId());
        verify(real).checkoutWithOptions(actor, List.of(1, 2), "CASH", "note", "Ship", "n", "p", "a");
    }

    private Customer customer(int id) {
        Customer c = new Customer();
        c.setId(id);
        return c;
    }

    private CartItem cartItem(int itemId, int ownerId) {
        Customer owner = customer(ownerId);
        Cart cart = new Cart();
        cart.setCustomer(owner);
        CartItem item = new CartItem();
        item.setId(itemId);
        item.setCart(cart);
        return item;
    }
}