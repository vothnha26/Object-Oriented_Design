package com.alotra.controller;

import com.alotra.dto.CartItemDTO;
import com.alotra.dto.ProductDTO;
import com.alotra.entity.Product;
import com.alotra.entity.ProductVariant;
import com.alotra.entity.Topping;
import com.alotra.service.interaction.CartService;
import com.alotra.service.product.ProductFacade;
import com.alotra.repository.ProductVariantRepository;
import com.alotra.repository.ToppingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ProductVariantRepository variantRepository;
    private final ToppingRepository toppingRepository;

    public CartController(CartService cartService, 
                          ProductVariantRepository variantRepository,
                          ToppingRepository toppingRepository) {
        this.cartService = cartService;
        this.variantRepository = variantRepository;
        this.toppingRepository = toppingRepository;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        List<CartItemDTO> cartItems = cartService.getCart(session);
        
        // Enrich DTOs with display information
        List<CartItemView> viewItems = new ArrayList<>();
        BigDecimal subTotal = BigDecimal.ZERO;

        for (CartItemDTO item : cartItems) {
            ProductVariant variant = variantRepository.findById(item.getVariantId()).orElse(null);
            if (variant == null) continue;

            CartItemView view = new CartItemView();
            view.setVariantId(variant.getId());
            view.setProductName(variant.getProduct().getName());
            view.setSizeName(variant.getSize().getName());
            view.setUnitPrice(variant.getPrice());
            view.setQuantity(item.getQuantity());
            view.setImageUrl(variant.getProduct().getImageUrl());

            // Load Toppings
            List<ToppingInfo> toppings = new ArrayList<>();
            BigDecimal toppingsTotal = BigDecimal.ZERO;
            if (item.getToppingIds() != null) {
                for (Integer tId : item.getToppingIds()) {
                    Topping t = toppingRepository.findById(tId).orElse(null);
                    if (t != null) {
                        toppings.add(new ToppingInfo(t.getName(), t.getExtraPrice()));
                        toppingsTotal = toppingsTotal.add(t.getExtraPrice());
                    }
                }
            }
            view.setToppings(toppings);
            
            BigDecimal itemTotal = variant.getPrice().add(toppingsTotal).multiply(BigDecimal.valueOf(item.getQuantity()));
            view.setTotal(itemTotal);
            subTotal = subTotal.add(itemTotal);
            
            viewItems.add(view);
        }

        model.addAttribute("cartItems", viewItems);
        model.addAttribute("subTotal", subTotal);
        model.addAttribute("pageTitle", "Giỏ hàng của bạn");
        
        return "cart/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session) {
        cartService.clearCart(session);
        return "redirect:/cart";
    }

    // Helper classes for view
    public static class CartItemView {
        private Integer variantId;
        private String productName;
        private String sizeName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal total;
        private String imageUrl;
        private List<ToppingInfo> toppings;

        // Getters/Setters
        public Integer getVariantId() { return variantId; }
        public void setVariantId(Integer variantId) { this.variantId = variantId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getSizeName() { return sizeName; }
        public void setSizeName(String sizeName) { this.sizeName = sizeName; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public List<ToppingInfo> getToppings() { return toppings; }
        public void setToppings(List<ToppingInfo> toppings) { this.toppings = toppings; }
    }

    public static class ToppingInfo {
        private String name;
        private BigDecimal price;
        public ToppingInfo(String name, BigDecimal price) { this.name = name; this.price = price; }
        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
    }
}
