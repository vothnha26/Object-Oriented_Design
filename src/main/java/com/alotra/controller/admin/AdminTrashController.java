package com.alotra.controller.admin;

import com.alotra.entity.*;
import com.alotra.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin/trash")
public class AdminTrashController {
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final EmployeeRepository employeeRepo;
    private final ToppingRepository toppingRepo;
    private final PromotionRepository promotionRepo;

    public AdminTrashController(ProductRepository productRepo,
                               CategoryRepository categoryRepo,
                               EmployeeRepository employeeRepo,
                               ToppingRepository toppingRepo,
                               PromotionRepository promotionRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.employeeRepo = employeeRepo;
        this.toppingRepo = toppingRepo;
        this.promotionRepo = promotionRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "Thùng rác");
        model.addAttribute("currentPage", "trash");
        model.addAttribute("deletedProducts", productRepo.findByDeletedAtIsNotNull());
        model.addAttribute("deletedCategories", categoryRepo.findByDeletedAtIsNotNull());
        model.addAttribute("deletedEmployees", employeeRepo.findByDeletedAtIsNotNull());
        model.addAttribute("deletedToppings", toppingRepo.findByDeletedAtIsNotNull());
        model.addAttribute("deletedPromotions", promotionRepo.findByDeletedAtIsNotNull());
        return "admin/trash";
    }

    @PostMapping("/products/{id}/restore")
    public String restoreProduct(@PathVariable Integer id, RedirectAttributes ra) {
        productRepo.findById(id).ifPresent(p -> {
            p.setDeletedAt(null);
            productRepo.save(p);
        });
        ra.addFlashAttribute("message", "Đã khôi phục sản phẩm");
        return "redirect:/admin/trash";
    }

    @PostMapping("/categories/{id}/restore")
    public String restoreCategory(@PathVariable Integer id, RedirectAttributes ra) {
        categoryRepo.findById(id).ifPresent(c -> {
            c.setDeletedAt(null);
            categoryRepo.save(c);
        });
        ra.addFlashAttribute("message", "Đã khôi phục danh mục");
        return "redirect:/admin/trash";
    }
}
