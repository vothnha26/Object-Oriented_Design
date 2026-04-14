package com.alotra.controller.vendor;

import com.alotra.entity.Promotion;
import com.alotra.entity.enums.PromotionStatus;
import com.alotra.repository.PromotionRepository;
import com.alotra.service.infrastructure.CloudinaryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/vendor/promotions")
public class VendorPromotionController {
    private final PromotionRepository promotionRepo;
    private final CloudinaryService cloudinaryService;

    public VendorPromotionController(PromotionRepository promotionRepo,
                                    CloudinaryService cloudinaryService) {
        this.promotionRepo = promotionRepo;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public String list(Model model) {
        List<Promotion> items = promotionRepo.findAll();
        model.addAttribute("items", items);
        model.addAttribute("pageTitle", "Khuyến mãi");
        model.addAttribute("currentPage", "vendor-promotions");
        return "vendor/promotion-list";
    }

    @GetMapping({"/add", "/new"})
    public String addForm(Model model) {
        model.addAttribute("pageTitle", "Thêm khuyến mãi");
        model.addAttribute("currentPage", "vendor-promotions");
        Promotion p = new Promotion();
        model.addAttribute("item", p);
        return "vendor/promotion-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Promotion p = promotionRepo.findById(id).orElseThrow();
        model.addAttribute("pageTitle", "Sửa khuyến mãi");
        model.addAttribute("currentPage", "vendor-promotions");
        model.addAttribute("item", p);
        return "vendor/promotion-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Promotion promotion,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       RedirectAttributes ra) {
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadFile(imageFile);
                promotion.setImageUrl(imageUrl);
            } catch (Exception e) {
                ra.addFlashAttribute("error", "Lỗi tải ảnh: " + e.getMessage());
                return promotion.getId() == null ? "redirect:/vendor/promotions/new" : "redirect:/vendor/promotions/edit/" + promotion.getId();
            }
        } else if (promotion.getId() != null) {
            promotionRepo.findById(promotion.getId()).ifPresent(old -> {
                if (promotion.getImageUrl() == null || promotion.getImageUrl().isBlank()) {
                    promotion.setImageUrl(old.getImageUrl());
                }
            });
        }

        if (promotion.getStatus() == null) promotion.setStatus(PromotionStatus.ACTIVE);
        promotionRepo.save(promotion);
        ra.addFlashAttribute("message", "Đã lưu khuyến mãi thành công");
        return "redirect:/vendor/promotions";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        promotionRepo.findById(id).ifPresent(p -> {
            p.setStatus(PromotionStatus.INACTIVE);
            promotionRepo.save(p);
        });
        ra.addFlashAttribute("message", "Đã chuyển khuyến mãi thành ngừng hoạt động");
        return "redirect:/vendor/promotions";
    }
}
