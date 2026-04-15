package com.alotra.util;

import com.alotra.dto.CartItemDTO;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

public class CartUtils {
    private static final String CART_SESSION_KEY = SessionKeys.getShoppingCart();

    @SuppressWarnings("unchecked")
    public static List<CartItemDTO> getCart(HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public static void saveCart(HttpSession session, List<CartItemDTO> cart) {
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public static void addToCart(List<CartItemDTO> cart, CartItemDTO newItem) {
        for (CartItemDTO item : cart) {
            if (item.getVariantId().equals(newItem.getVariantId()) && isSameToppings(item.getToppingIds(), newItem.getToppingIds())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                return;
            }
        }
        cart.add(newItem);
    }

    private static boolean isSameToppings(List<Integer> list1, List<Integer> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null || list1.size() != list2.size()) return false;
        return list1.containsAll(list2);
    }

    public static void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }
}
