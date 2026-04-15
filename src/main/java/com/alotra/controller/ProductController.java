package com.alotra.controller;

import com.alotra.dto.CartItemDTO;
import com.alotra.dto.ReviewDto;
import com.alotra.entity.Product;
import com.alotra.entity.ProductSize;
import com.alotra.entity.ProductVariant;
import com.alotra.entity.Topping;
import com.alotra.repository.ProductVariantRepository;
import com.alotra.service.interaction.CartService;
import com.alotra.service.product.ProductFacade;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductVariantRepository variantRepository;

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Product product = productFacade.getProductDetail(id);
        List<ProductVariant> variants = productFacade.getActiveVariants(product);
        List<Topping> toppings = productFacade.getAvailableToppings();
        List<ReviewDto> reviews = productFacade.getProductReviews(id);

        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("toppings", toppings);
        model.addAttribute("reviews", reviews);

        return "products/product_detail";
    }

    @PostMapping("/{id}/add-to-cart")
    public String addToCart(@PathVariable Integer id,
                            @RequestParam Integer variantId,
                            @RequestParam(defaultValue = "1") Integer qty,
                            @RequestParam(required = false) String sugar,
                            @RequestParam(required = false) String ice,
                            @RequestParam Map<String, String> allParams,
                            HttpSession session) {
        
        ProductVariant variant = variantRepository.findById(variantId).orElseThrow(() -> new IllegalArgumentException("Biến thể không tồn tại"));
        Product product = variant.getProduct();
        ProductSize size = variant.getSize();

        CartItemDTO item = new CartItemDTO();
        item.setVariantId(variantId);
        item.setQuantity(qty);
        item.setVariantName(product.getName());
        item.setSizeName(size.getName());
        item.setPrice(variant.getPrice());
        
        StringBuilder note = new StringBuilder();
        if (sugar != null && !sugar.isEmpty()) note.append("Đường: ").append(sugar).append("; ");
        if (ice != null && !ice.isEmpty()) note.append("Đá: ").append(ice);
        item.setNote(note.toString().trim());

        List<Integer> toppingIds = new ArrayList<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("toppings[") && entry.getKey().endsWith("]")) {
                try {
                    String idStr = entry.getKey().substring("toppings[".length(), entry.getKey().length() - 1);
                    int toppingId = Integer.parseInt(idStr);
                    int toppingQty = Integer.parseInt(entry.getValue());
                    for (int i = 0; i < toppingQty; i++) {
                        toppingIds.add(toppingId);
                    }
                } catch (Exception ignored) {}
            }
        }
        item.setToppingIds(toppingIds);

        cartService.addToCart(session, item);
        return "redirect:/cart";
    }
}
