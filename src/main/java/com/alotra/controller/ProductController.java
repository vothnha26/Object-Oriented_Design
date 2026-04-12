package com.alotra.controller;

import com.alotra.entity.*;
import com.alotra.dto.CartItemDTO;
import com.alotra.service.product.ProductFacade;
import com.alotra.service.interaction.WishlistOperations;
import com.alotra.service.interaction.CartService;
import com.alotra.security.CustomerUserDetails;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductFacade productFacade;
    private final WishlistOperations wishlistProxy;
    private final CartService cartService;

    public ProductController(ProductFacade productFacade, 
                             WishlistOperations wishlistProxy,
                             CartService cartService) {
        this.productFacade = productFacade;
        this.wishlistProxy = wishlistProxy;
        this.cartService = cartService;
    }

    @GetMapping("/{id}")
    public String detail(@AuthenticationPrincipal CustomerUserDetails principal, @PathVariable Integer id, Model model) {
        Product p = productFacade.getProductDetail(id);
        List<ProductVariant> variants = productFacade.getActiveVariants(p);
        List<Topping> toppings = productFacade.getAvailableToppings();
        List<Review> reviews = productFacade.getProductReviews(id);

        boolean isInWishlist = false;
        if (principal != null) {
            isInWishlist = wishlistProxy.isInWishlist(principal.getCustomer(), id);
        }

        model.addAttribute("pageTitle", p.getName());
        model.addAttribute("product", p);
        model.addAttribute("variants", variants);
        model.addAttribute("toppings", toppings);
        model.addAttribute("basePrice", !variants.isEmpty() ? variants.get(0).getPrice() : BigDecimal.ZERO);
        model.addAttribute("reviews", reviews);
        model.addAttribute("isInWishlist", isInWishlist);
        model.addAttribute("principal", principal);
        return "products/product_detail";
    }

    @PostMapping("/{id}/add-to-cart")
    public String addToCart(@PathVariable Integer id,
                           @RequestParam Integer variantId,
                           @RequestParam(name = "qty", defaultValue = "1") Integer quantity,
                           @RequestParam(required = false) Map<String, String> allParams,
                           HttpSession session) {
        try {
            CartItemDTO item = new CartItemDTO();
            item.setVariantId(variantId);
            item.setQuantity(quantity);
            
            List<Integer> toppingIds = new ArrayList<>();
            if (allParams != null) {
                for (Map.Entry<String, String> entry : allParams.entrySet()) {
                    if (entry.getKey().startsWith("toppings[")) {
                        int count = Integer.parseInt(entry.getValue());
                        if (count > 0) {
                            String tIdStr = entry.getKey().substring(9, entry.getKey().length() - 1);
                            int tId = Integer.parseInt(tIdStr);
                            for (int i = 0; i < count; i++) {
                                toppingIds.add(tId);
                            }
                        }
                    }
                }
            }
            item.setToppingIds(toppingIds);
            
            cartService.addToCart(session, item);
            return "redirect:/cart";
        } catch (Exception e) {
            return "redirect:/products/" + id + "?error";
        }
    }
}
