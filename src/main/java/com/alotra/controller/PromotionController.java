package com.alotra.controller;

import com.alotra.entity.Promotion;
import com.alotra.entity.AppliedPromotion;
import com.alotra.repository.PromotionRepository;
import com.alotra.repository.AppliedPromotionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/promotions")
public class PromotionController {
    private final PromotionRepository promotionRepository;
    private final AppliedPromotionRepository appliedPromotionRepository;

    public PromotionController(PromotionRepository promotionRepository, AppliedPromotionRepository appliedPromotionRepository) {
        this.promotionRepository = promotionRepository;
        this.appliedPromotionRepository = appliedPromotionRepository;
    }

    @GetMapping
    public String list(Model model) {
        List<Promotion> items = promotionRepository.findTop8ByStatusAndDeletedAtIsNullOrderByStartDateDesc(com.alotra.entity.enums.PromotionStatus.ACTIVE);
        model.addAttribute("pageTitle", "Tin tức & Khuyến mãi");
        model.addAttribute("items", items);
        return "promotions/list";
    }

    @GetMapping("/{id}")
    @Transactional
    public String detail(@PathVariable Integer id, Model model) {
        Promotion p = promotionRepository.findById(id).orElse(null);
        if (p == null) return "redirect:/promotions";
        
        promotionRepository.incrementViews(id);
        Integer currentViews = p.getViews();
        p.setViews((currentViews == null ? 0 : currentViews) + 1);
        
        List<AppliedPromotion> assigned = appliedPromotionRepository.findByPromotion(p);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String period = (p.getStartDate() != null ? df.format(p.getStartDate()) : "?") +
                " - " + (p.getEndDate() != null ? df.format(p.getEndDate()) : "?");
        
        model.addAttribute("item", p);
        model.addAttribute("period", period);
        model.addAttribute("assigned", assigned);
        model.addAttribute("pageTitle", p.getName());
        return "promotions/detail";
    }
}