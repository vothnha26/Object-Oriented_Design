package com.alotra.controller.account;

import com.alotra.entity.Customer;
import com.alotra.entity.Product;
import com.alotra.entity.Wishlist;
import com.alotra.security.CustomerUserDetails;
import com.alotra.service.account.CustomerService;
import com.alotra.service.interaction.WishlistOperations;
import com.alotra.service.product.ProductFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/account/wishlist")
public class WishlistController {
    private final WishlistOperations wishlistProxy;
    private final CustomerService customerService;
    private final ProductFacade productFacade;

    public WishlistController(WishlistOperations wishlistProxy, 
                              CustomerService customerService, 
                              ProductFacade productFacade) {
        this.wishlistProxy = wishlistProxy;
        this.customerService = customerService;
        this.productFacade = productFacade;
    }

    @GetMapping
    public String showWishlist(@AuthenticationPrincipal CustomerUserDetails principal, Model model) {
        Customer customer = customerService.findById(principal.getId());
        List<Wishlist> wishlist = wishlistProxy.getCustomerWishlist(customer);
        model.addAttribute("wishlist", wishlist);
        model.addAttribute("pageTitle", "Danh sách yêu thích");
        return "account/wishlist";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addToWishlist(@AuthenticationPrincipal CustomerUserDetails principal, 
                                           @RequestParam Integer productId) {
        try {
            Customer customer = customerService.findById(principal.getId());
            Product product = productFacade.getProductDetail(productId);
            wishlistProxy.addToWishlist(customer, product);
            return ResponseEntity.ok(Map.of("message", "Đã thêm vào danh sách yêu thích"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<?> removeFromWishlist(@AuthenticationPrincipal CustomerUserDetails principal, 
                                                @RequestParam Integer productId) {
        Customer customer = customerService.findById(principal.getId());
        wishlistProxy.removeFromWishlist(customer, productId);
        return ResponseEntity.ok(Map.of("message", " Đã xóa khỏi danh sách yêu thích"));
    }
}
