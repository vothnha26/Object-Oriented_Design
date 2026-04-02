package com.alotra.controller.admin;

import com.alotra.entity.Promotion;
import com.alotra.entity.enums.PromotionStatus;
import com.alotra.repository.PromotionRepository;
import com.alotra.service.CloudinaryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

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
        List<Promotion> items = promotionRepo.findByDeletedAtIsNull();
        model.addAttribute("items", items);
        model.addAttribute("pageTitle", "Khuyến mãi");
        model.addAttribute("currentPage", "promotions");
        return "admin/promotion-list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("pageTitle", "Thêm khuyến mãi");
        model.addAttribute("currentPage", "promotions");
        model.addAttribute("promotion", new Promotion());
        return "admin/promotion-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Promotion p = promotionRepo.findById(id).orElseThrow();
        model.addAttribute("pageTitle", "Sửa khuyến mãi");
        model.addAttribute("currentPage", "promotions");
        model.addAttribute("promotion", p);
        return "admin/promotion-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Promotion promotion,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       RedirectAttributes ra) {
        if (imageFile != null && !imageFile.isEmpty()) {
            String url = cloudinaryService.uploadFile(imageFile);
            promotion.setImageUrl(url);
        }
        if (promotion.getStatus() == null) promotion.setStatus(PromotionStatus.ACTIVE);
        promotionRepo.save(promotion);
        ra.addFlashAttribute("message", "Đã lưu khuyến mãi");
        return "redirect:/admin/promotions";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        promotionRepo.findById(id).ifPresent(p -> {
            p.setDeletedAt(java.time.LocalDateTime.now());
            promotionRepo.save(p);
        });
        ra.addFlashAttribute("message", "Đã chuyển khuyến mãi vào thùng rác");
        return "redirect:/admin/promotions";
    }
}
