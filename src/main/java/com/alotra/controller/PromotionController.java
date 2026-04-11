package com.alotra.controller;

import com.alotra.entity.Promotion;
import com.alotra.repository.PromotionRepository;
import com.alotra.entity.enums.PromotionStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/promotions")
public class PromotionController {
    private final PromotionRepository promotionRepo;

    public PromotionController(PromotionRepository promotionRepo) {
        this.promotionRepo = promotionRepo;
    }

    @GetMapping
    public String list(Model model) {
        List<Promotion> items = promotionRepo.findByStatus(PromotionStatus.ACTIVE);
        model.addAttribute("items", items);
        model.addAttribute("pageTitle", "Chương trình khuyến mãi");
        return "promotions/promotion_list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Promotion p = promotionRepo.findById(id).orElseThrow();
        model.addAttribute("promotion", p);
        model.addAttribute("pageTitle", p.getCode());
        return "promotions/promotion_detail";
    }
}
