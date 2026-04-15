package com.alotra.controller;

import com.alotra.dto.CartItemDTO;
import com.alotra.entity.Order;
import com.alotra.repository.ProductRepository;
import com.alotra.repository.ProductVariantRepository;
import com.alotra.repository.ProductSizeRepository;
import com.alotra.service.interaction.CartService;
import com.alotra.service.order.OrderFactory;
import com.alotra.service.order.PriceService;
import com.alotra.service.pricing.PricingService;
import com.alotra.security.CustomerUserDetails;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderFactory orderFactory;

    @Autowired
    private PriceService priceService;

    @Autowired
    private ProductVariantRepository variantRepository;
    
    @Autowired
    private ProductSizeRepository sizeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PricingService pricingService;

    @GetMapping
    public String viewCart(HttpSession session, 
                           @AuthenticationPrincipal CustomerUserDetails principal,
                           @RequestParam(required = false) String promoCode,
                           Model model) {
        List<CartItemDTO> cartItems = cartService.getCart(session);
        
        if (cartItems.isEmpty()) {
            model.addAttribute("cartItems", cartItems);
            return "cart/cart";
        }

        // Tạo order tạm để tính giá chuẩn xác bằng PriceService
        Order tempOrder = orderFactory.createOrder(
                principal != null ? principal.getCustomer() : null,
                cartItems,
                "");
        
        priceService.calculateTotal(tempOrder, promoCode);

        // Sử dụng PricingService để lấy các thành phần giá đã được tách biệt
        BigDecimal subTotal = pricingService.calculateSubtotal(tempOrder);
        BigDecimal finalTotal = tempOrder.getFinalTotal();
        BigDecimal discountAmount = subTotal.subtract(finalTotal);

        model.addAttribute("cartItems", tempOrder.getItems());
        model.addAttribute("subTotal", subTotal);
        model.addAttribute("discountAmount", discountAmount);
        model.addAttribute("totalAmount", finalTotal);
        model.addAttribute("promoCode", promoCode);
        
        // Thêm helper để hiển thị tên item (do OrderItem không còn phương thức helper)
        model.addAttribute("pricingService", pricingService);
        
        // Helper repositories for template
        model.addAttribute("sizeRepository", sizeRepository);
        model.addAttribute("variantRepository", variantRepository);
        model.addAttribute("productRepository", productRepository);
        
        return "cart/cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public String addToCart(@RequestBody CartItemDTO item, HttpSession session) {
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            item.setQuantity(1);
        }
        
        cartService.addToCart(session, item);
        return "success";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session) {
        cartService.clearCart(session);
        return "redirect:/cart";
    }
}
