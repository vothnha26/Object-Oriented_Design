package com.alotra.controller.api;

import com.alotra.entity.Customer;
import com.alotra.security.CustomerUserDetails;
import com.alotra.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {
    private final CartService cartService;

    public CartApiController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody AddRequest req, Authentication auth) {
        Customer c = getCurrentCustomer(auth);
        if (c == null) {
            return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        }
        Integer qty = (req.quantity == null || req.quantity <= 0) ? 1 : req.quantity;
        if (req.productId == null && req.variantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing productId or variantId"));
        }
        cartService.addItemWithOptions(c, req.variantId, qty, null, null);
        int count = cartService.getItemCount(c);
        return ResponseEntity.ok(Map.of("ok", true, "count", count));
    }

    @GetMapping("/count")
    public Map<String, Object> count(Authentication auth) {
        Customer c = getCurrentCustomer(auth);
        int count = (c == null) ? 0 : cartService.getItemCount(c);
        return Map.of("count", count);
    }

    private Customer getCurrentCustomer(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null;
        Object p = auth.getPrincipal();
        if (p instanceof CustomerUserDetails cud) {
            return cud.getCustomer();
        }
        return null;
    }

    public static class AddRequest {
        public Integer productId;
        public Integer variantId;
        public Integer quantity;
    }
}
