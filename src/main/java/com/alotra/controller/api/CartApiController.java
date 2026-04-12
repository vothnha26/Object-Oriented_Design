package com.alotra.controller.api;

import com.alotra.dto.CartItemDTO;
import com.alotra.service.interaction.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {
    private final CartService cartService;

    public CartApiController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/count")
    public ResponseEntity<?> getCartCount(HttpSession session) {
        List<CartItemDTO> cart = cartService.getCart(session);
        int count = cart.stream().mapToInt(CartItemDTO::getQuantity).sum();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(HttpSession session, @RequestBody CartItemDTO item) {
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            item.setQuantity(1);
        }
        cartService.addToCart(session, item);
        List<CartItemDTO> cart = cartService.getCart(session);
        int count = cart.stream().mapToInt(CartItemDTO::getQuantity).sum();
        return ResponseEntity.ok(Map.of("message", "Đã thêm vào giỏ hàng", "count", count));
    }

    @PostMapping("/clear")
    public ResponseEntity<?> clearCart(HttpSession session) {
        cartService.clearCart(session);
        return ResponseEntity.ok(Map.of("message", "Đã xóa giỏ hàng"));
    }
}
