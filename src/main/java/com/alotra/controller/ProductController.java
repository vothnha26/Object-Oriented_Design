package com.alotra.controller;

import com.alotra.entity.*;
import com.alotra.service.product.ProductFacade;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductFacade productFacade;

    public ProductController(ProductFacade productFacade) {
        this.productFacade = productFacade;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Product p = productFacade.getProductDetail(id);
        List<ProductVariant> variants = productFacade.getActiveVariants(p);
        List<Topping> toppings = productFacade.getAvailableToppings();
        List<Review> reviews = productFacade.getProductReviews(id);

        model.addAttribute("pageTitle", p.getName());
        model.addAttribute("product", p);
        model.addAttribute("variants", variants);
        model.addAttribute("toppings", toppings);
        model.addAttribute("basePrice", !variants.isEmpty() ? variants.get(0).getPrice() : BigDecimal.ZERO);
        model.addAttribute("reviews", reviews);
        return "products/product_detail";
    }
}
