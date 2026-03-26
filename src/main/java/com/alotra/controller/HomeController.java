package com.alotra.controller;

import com.alotra.dto.ProductDTO;
import com.alotra.entity.Promotion;
import com.alotra.repository.PromotionRepository;
import com.alotra.repository.ProductPromotionRepository;
import com.alotra.service.ProductService;
import com.alotra.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private ProductService productService;
    @Autowired 
    private PromotionRepository promotionRepository;
    @Autowired 
    private ProductPromotionRepository productPromotionRepository;
    @Autowired 
    private CategoryService categoryService;

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("pageTitle", "AloTra - Trang Chủ");
        List<ProductDTO> bestSellers = productService.findBestSellers();
        model.addAttribute("bestSellers", bestSellers);

        List<Promotion> promos = promotionRepository.findTop8ByStatusAndDeletedAtIsNullOrderByStartDateDesc(com.alotra.entity.enums.PromotionStatus.ACTIVE);
        List<PromotionCard> cards = new ArrayList<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Promotion p : promos) {
            String eventImg = (p.getImageUrl() != null && !p.getImageUrl().isBlank()) ? p.getImageUrl() : null;
            String fallbackImg = productPromotionRepository.findByPromotion(p).stream()
                    .map(l -> l.getProduct())
                    .filter(pr -> pr != null && pr.getImageUrl() != null && !pr.getImageUrl().isBlank())
                    .map(pr -> pr.getImageUrl())
                    .findFirst()
                    .orElse(null);
            String imageUrl = eventImg != null ? eventImg : (fallbackImg != null ? fallbackImg : "/images/placeholder.png");
            String period = (p.getStartDate() != null ? df.format(p.getStartDate()) : "?") +
                    " - " + (p.getEndDate() != null ? df.format(p.getEndDate()) : "?");
            int views = p.getViews() == null ? 0 : p.getViews();
            cards.add(new PromotionCard(p.getId(), p.getName(), p.getDescription(), imageUrl, period, views));
        }
        model.addAttribute("promotions", cards);
        return "home/index";
    }

    @GetMapping("/products")
    public String productsPage(@RequestParam(required = false) Integer categoryId,
                               @RequestParam(required = false) String search,
                               Model model) {
        model.addAttribute("pageTitle", "Sản Phẩm của AloTra");
        var categories = categoryService.findActive();
        model.addAttribute("categories", categories);
        List<ProductDTO> initial = productService.listByCategoryAndSearch(categoryId, search);
        model.addAttribute("products", initial);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("search", search);
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
        public int views;
        public PromotionCard(Integer id, String title, String description, String imageUrl, String periodText, int views) {
            this.id = id; this.title = title; this.description = description; this.imageUrl = imageUrl; this.periodText = periodText; this.views = views;
        }
    }
}