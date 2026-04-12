package com.alotra.service.interaction;

import com.alotra.dto.CartItemDTO;
import com.alotra.util.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {
    private static final String CART_SESSION_KEY = SessionKeys.getShoppingCart();

    @SuppressWarnings("unchecked")
    public List<CartItemDTO> getCart(HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public void addToCart(HttpSession session, CartItemDTO newItem) {
        List<CartItemDTO> cart = getCart(session);
        
        // Kiểm tra xem sản phẩm cùng variant và topping đã tồn tại chưa
        boolean found = false;
        for (CartItemDTO item : cart) {
            if (item.getVariantId().equals(newItem.getVariantId()) && 
                isSameToppings(item.getToppingIds(), newItem.getToppingIds())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                found = true;
                break;
            }
        }
        
        if (!found) {
            cart.add(newItem);
        }
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void removeFromCart(HttpSession session, Integer variantId) {
        List<CartItemDTO> cart = getCart(session);
        cart.removeIf(item -> item.getVariantId().equals(variantId));
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    private boolean isSameToppings(List<Integer> list1, List<Integer> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null || list1.size() != list2.size()) return false;
        return list1.containsAll(list2);
    }
}
