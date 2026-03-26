package com.alotra.controller;

import com.alotra.entity.*;
import com.alotra.repository.ProductRepository;
import com.alotra.repository.ProductVariantRepository;
import com.alotra.repository.ToppingRepository;
import com.alotra.repository.AppliedPromotionRepository;
import com.alotra.security.CustomerUserDetails;
import com.alotra.service.CartService;
import com.alotra.service.CustomerService;
import com.alotra.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final ToppingRepository toppingRepo;
    private final CartService cartService;
    private final CustomerService customerService;
    private final AppliedPromotionRepository appliedPromotionRepository;
    private final ReviewService reviewService;

    public ProductController(ProductRepository productRepo,
                             ProductVariantRepository variantRepo,
                             ToppingRepository toppingRepo,
                             CartService cartService,
                             CustomerService customerService,
                             AppliedPromotionRepository appliedPromotionRepository,
                             ReviewService reviewService) {
        this.productRepo = productRepo;
        this.variantRepo = variantRepo;
        this.toppingRepo = toppingRepo;
        this.cartService = cartService;
        this.customerService = customerService;
        this.appliedPromotionRepository = appliedPromotionRepository;
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Product p = productRepo.findById(id).orElseThrow();
        List<ProductVariant> variants = variantRepo.findByProduct(p);
        variants.removeIf(v -> !v.isActive());
        variants.sort(Comparator.comparing(ProductVariant::getPrice));
        List<Topping> toppings = toppingRepo.findByDeletedAtIsNull();
        toppings.removeIf(t -> !t.isAvailable());

        Integer discountPercent = appliedPromotionRepository.findActiveMaxDiscountPercentForProduct(p.getId());
        BigDecimal basePrice = (!variants.isEmpty() ? variants.get(0).getPrice() : BigDecimal.ZERO);
        BigDecimal discountedPrice = applyPercent(basePrice, discountPercent);

        List<Review> reviews = reviewService.listByProduct(id, null);

        model.addAttribute("pageTitle", p.getName());
        model.addAttribute("product", p);
        model.addAttribute("variants", variants);
        model.addAttribute("toppings", toppings);
        model.addAttribute("discountPercent", discountPercent);
        model.addAttribute("basePrice", basePrice);
        model.addAttribute("discountedPrice", discountedPrice);
        model.addAttribute("reviews", reviews);
        return "products/product_detail";
    }

    private BigDecimal applyPercent(BigDecimal base, Integer percent) {
        if (base == null) return null;
        if (percent == null || percent <= 0) return base;
        BigDecimal factor = BigDecimal.valueOf(100 - Math.min(100, percent))
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return base.multiply(factor).setScale(0, RoundingMode.HALF_UP);
    }

    @PostMapping("/{id}/add-to-cart")
    public String addToCart(@PathVariable Integer id,
                            @RequestParam("variantId") Integer variantId,
                            @RequestParam(value = "qty", defaultValue = "1") Integer qty,
                            @RequestParam(value = "sugar", defaultValue = "Bình thường") String sugar,
                            @RequestParam(value = "ice", defaultValue = "Bình thường") String ice,
                            HttpServletRequest request,
                            @AuthenticationPrincipal CustomerUserDetails principal,
                            RedirectAttributes ra) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Customer customer = null;
        if (principal != null) customer = principal.getCustomer();
        if (customer == null && auth != null && auth.isAuthenticated() && auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
            customer = customerService.findByUsername(auth.getName());
        }
        if (customer == null) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng");
            return "redirect:/login";
        }
        Map<Integer, Integer> toppingQty = extractToppings(request);
        String note = String.format("Đường: %s; Đá: %s", sugar, ice);
        try {
            cartService.addItemWithOptions(customer, variantId, qty, toppingQty, note);
            ra.addFlashAttribute("message", "Đã thêm vào giỏ hàng");
            return "redirect:/";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/products/" + id;
        }
    }

    private Map<Integer, Integer> extractToppings(HttpServletRequest request) {
        Map<Integer, Integer> map = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (k.startsWith("toppings[") && k.endsWith("]")) {
                try {
                    String idStr = k.substring(9, k.length() - 1);
                    Integer tid = Integer.parseInt(idStr);
                    Integer q = Integer.parseInt(v[0]);
                    if (q != null && q > 0) map.put(tid, q);
                } catch (Exception ignored) { }
            }
        });
        return map;
    }
}