package com.alotra.controller;

import com.alotra.entity.Promotion;
import com.alotra.service.marketing.PromotionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/promotions")
public class PromotionController {
    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public String list(Model model) {
        List<Promotion> items = promotionService.getActivePromotions();
        model.addAttribute("items", items);
        model.addAttribute("pageTitle", "Chương trình khuyến mãi");
        return "promotions/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Promotion p = promotionService.findById(id).orElseThrow();
        
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String period = p.getStartDate().format(dtf) + " - " + p.getEndDate().format(dtf);
        
        model.addAttribute("item", p);
        model.addAttribute("period", period);
        model.addAttribute("pageTitle", p.getName());
        return "promotions/detail";
    }
}
