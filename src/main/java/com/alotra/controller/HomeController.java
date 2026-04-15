package com.alotra.controller;

import com.alotra.dto.ProductDTO;
import com.alotra.entity.Promotion;
import com.alotra.repository.PromotionRepository;
import com.alotra.service.product.ProductFacade;
import com.alotra.service.product.CategoryService;
import com.alotra.service.interaction.WishlistOperations;
import com.alotra.security.CustomerUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {
    private final ProductFacade productFacade;
    private final PromotionRepository promotionRepository;
    private final CategoryService categoryService;
    private final WishlistOperations wishlistProxy;

    public HomeController(ProductFacade productFacade,
            PromotionRepository promotionRepository,
            CategoryService categoryService,
            WishlistOperations wishlistProxy) {
        this.productFacade = productFacade;
        this.promotionRepository = promotionRepository;
        this.categoryService = categoryService;
        this.wishlistProxy = wishlistProxy;
    }

    @GetMapping("/")
    public String homePage(@AuthenticationPrincipal CustomerUserDetails principal, Model model) {
        model.addAttribute("pageTitle", "AloTra - Trang Chủ");
        
        // Cần thiết cho Header Fragment
        model.addAttribute("megaMenuCategories", categoryService.findActive());

        List<ProductDTO> bestSellers = productFacade.getHomeProducts();
        model.addAttribute("bestSellers", bestSellers);

        // Fetch wishlist product IDs if logged in
        List<Integer> wishlistProductIds = Collections.emptyList();
        if (principal != null) {
            wishlistProductIds = wishlistProxy.getCustomerWishlist(principal.getCustomer())
                    .stream().map(w -> w.getProduct().getId()).collect(Collectors.toList());
        }
        model.addAttribute("wishlistProductIds", wishlistProductIds);

        List<Promotion> promos = promotionRepository.findActivePromotions(java.time.LocalDate.now());
        List<PromotionCard> cards = new ArrayList<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Promotion p : promos) {
            if (!p.isActive())
                continue;
            String imageUrl = p.getImageUrl() != null ? p.getImageUrl() : "/images/placeholder.png";
            String period = "Đến " + (p.getEndDate() != null ? df.format(p.getEndDate()) : "?");
            cards.add(new PromotionCard(p.getId(), p.getName(), p.getDescription(), imageUrl, period));
        }
        model.addAttribute("promotions", cards);
        return "home/index";
    }

    @GetMapping("/products")
    public String productsPage(@AuthenticationPrincipal CustomerUserDetails principal,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String search,
            Model model) {
        model.addAttribute("pageTitle", "Sản Phẩm của AloTra");
        var categories = categoryService.findActive();
        model.addAttribute("categories", categories);
        List<ProductDTO> products = productFacade.searchProducts(categoryId, search);
        model.addAttribute("products", products);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("search", search);

        // Fetch wishlist product IDs if logged in
        List<Integer> wishlistProductIds = Collections.emptyList();
        if (principal != null) {
            wishlistProductIds = wishlistProxy.getCustomerWishlist(principal.getCustomer())
                    .stream().map(w -> w.getProduct().getId()).collect(Collectors.toList());
        }
        model.addAttribute("wishlistProductIds", wishlistProductIds);

        return "products/product_list";
    }

    @GetMapping("/about")
    public String aboutPage(Model model) {
        model.addAttribute("pageTitle", "Về Chúng Tôi");
        return "about/about";
    }

    @GetMapping("/contact")
    public String contactPage(Model model) {
        model.addAttribute("pageTitle", "Liên Hệ AloTra");
        return "contact/contact";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("pageTitle", "Đăng Nhập");
        return "auth/login";
    }

    @GetMapping("/policy")
    public String policyPage(Model model) {
        model.addAttribute("pageTitle", "Chính Sách");
        return "policy/policy";
    }

    public static class PromotionCard {
        public Integer id;
        public String title;
        public String description;
        public String imageUrl;
        public String periodText;

        public PromotionCard(Integer id, String title, String description, String imageUrl, String periodText) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.imageUrl = imageUrl;
            this.periodText = periodText;
        }
    }
}
