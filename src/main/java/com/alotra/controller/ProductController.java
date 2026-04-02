package com.alotra.controller;

import com.alotra.entity.*;
import com.alotra.repository.ProductRepository;
import com.alotra.repository.ProductVariantRepository;
import com.alotra.repository.ToppingRepository;
import com.alotra.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final ToppingRepository toppingRepo;
    private final ReviewService reviewService;

    public ProductController(ProductRepository productRepo,
                             ProductVariantRepository variantRepo,
                             ToppingRepository toppingRepo,
                             ReviewService reviewService) {
        this.productRepo = productRepo;
        this.variantRepo = variantRepo;
        this.toppingRepo = toppingRepo;
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

        List<Review> reviews = reviewService.listByProduct(id, null);

        model.addAttribute("pageTitle", p.getName());
        model.addAttribute("product", p);
        model.addAttribute("variants", variants);
        model.addAttribute("toppings", toppings);
        model.addAttribute("basePrice", !variants.isEmpty() ? variants.get(0).getPrice() : BigDecimal.ZERO);
        model.addAttribute("reviews", reviews);
        return "products/product_detail";
    }
}
