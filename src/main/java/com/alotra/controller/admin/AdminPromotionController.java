package com.alotra.controller.admin;

import com.alotra.entity.Promotion;
import com.alotra.repository.PromotionRepository;
import com.alotra.service.infrastructure.CloudinaryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/promotions")
public class AdminPromotionController {
    private final PromotionRepository promotionRepo;
    private final CloudinaryService cloudinaryService;

    public AdminPromotionController(PromotionRepository promotionRepo,
                                   CloudinaryService cloudinaryService) {
        this.promotionRepo = promotionRepo;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public String list(Model model) {
        // Fetch all promotions for the admin list
        List<Promotion> items = promotionRepo.findAll();
        model.addAttribute("items", items);
        model.addAttribute("pageTitle", "Khuyến mãi");
        model.addAttribute("currentPage", "promotions");
        return "admin/promotion-list";
    }

    @GetMapping({"/add", "/new"})
    public String addForm(Model model) {
        model.addAttribute("pageTitle", "Thêm khuyến mãi");
        model.addAttribute("currentPage", "promotions");
        Promotion p = new Promotion();
        model.addAttribute("promotion", p);
        model.addAttribute("item", p); // Match template variable name
        return "admin/promotion-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Promotion p = promotionRepo.findById(id).orElseThrow();
        model.addAttribute("pageTitle", "Sửa khuyến mãi");
        model.addAttribute("currentPage", "promotions");
        model.addAttribute("promotion", p);
        model.addAttribute("item", p); // Match template variable name
        return "admin/promotion-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Promotion promotion,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       RedirectAttributes ra) {
        
        // Xử lý upload ảnh nếu có file mới
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadFile(imageFile);
                promotion.setImageUrl(imageUrl);
            } catch (Exception e) {
                ra.addFlashAttribute("error", "Lỗi tải ảnh: " + e.getMessage());
                return "redirect:/admin/promotions/new";
            }
        } else if (promotion.getId() != null) {
            // Nếu là sửa và không upload ảnh mới, giữ lại ảnh cũ
            promotionRepo.findById(promotion.getId()).ifPresent(old -> {
                if (promotion.getImageUrl() == null || promotion.getImageUrl().isBlank()) {
                    promotion.setImageUrl(old.getImageUrl());
                }
            });
        }

        promotionRepo.save(promotion);
        ra.addFlashAttribute("message", "Đã lưu khuyến mãi thành công");
        return "redirect:/admin/promotions";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        // Physical delete or set end date to yesterday to deactivate
        promotionRepo.findById(id).ifPresent(p -> {
            p.setEndDate(java.time.LocalDate.now().minusDays(1));
            promotionRepo.save(p);
        });
        ra.addFlashAttribute("message", "Đã hủy kích hoạt khuyến mãi thành công");
        return "redirect:/admin/promotions";
    }
}
