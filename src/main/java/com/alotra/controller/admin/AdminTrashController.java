package com.alotra.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Collections;

@Controller
@RequestMapping("/admin/trash")
public class AdminTrashController {

    public AdminTrashController() {
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "Thùng rác");
        model.addAttribute("currentPage", "trash");
        model.addAttribute("deletedProducts", Collections.emptyList());
        model.addAttribute("deletedCategories", Collections.emptyList());
        model.addAttribute("deletedEmployees", Collections.emptyList());
        model.addAttribute("deletedToppings", Collections.emptyList());
        model.addAttribute("deletedPromotions", Collections.emptyList());
        return "admin/trash";
    }

    @PostMapping("/products/{id}/restore")
    public String restoreProduct(@PathVariable Integer id, RedirectAttributes ra) {
        ra.addFlashAttribute("error", "Chức năng khôi phục không khả dụng trong thiết kế mới.");
        return "redirect:/admin/trash";
    }

    @PostMapping("/categories/{id}/restore")
    public String restoreCategory(@PathVariable Integer id, RedirectAttributes ra) {
        ra.addFlashAttribute("error", "Chức năng khôi phục không khả dụng trong thiết kế mới.");
        return "redirect:/admin/trash";
    }
}
