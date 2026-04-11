package com.alotra.controller.admin;

import com.alotra.entity.Category;
import com.alotra.entity.Topping;
import com.alotra.entity.enums.ToppingStatus;
import com.alotra.repository.CategoryRepository;
import com.alotra.repository.ProductRepository;
import com.alotra.repository.ToppingRepository;
import com.alotra.storage.ImageStorageService;
import com.alotra.service.analytics.StatsService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final CategoryRepository categoryRepository;
    private final ToppingRepository toppingRepository;
    private final ImageStorageService storageService;
    private final ProductRepository productRepository;
    private final StatsService statsService;

    public AdminController(CategoryRepository categoryRepository, 
                           ToppingRepository toppingRepository, 
                           com.alotra.storage.StorageFactory storageFactory, 
                           ProductRepository productRepository, 
                           StatsService statsService) {
        this.categoryRepository = categoryRepository;
        this.toppingRepository = toppingRepository;
        this.storageService = storageFactory.getStorageService();
        this.productRepository = productRepository;
        this.statsService = statsService;
    }

    @GetMapping
    public String showAdminRoot() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        var stats = statsService.loadDashboardStats();
        model.addAttribute("pageTitle", "Tổng quan");
        model.addAttribute("currentPage", "dashboard");
        model.addAttribute("stats", stats);
        return "admin/dashboard";
    }

    @GetMapping("/categories")
    public String showCategories(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("pageTitle", "Danh mục");
        model.addAttribute("currentPage", "categories");
        model.addAttribute("categoryList", categories);
        return "admin/categories";
    }

    @GetMapping("/categories/add")
    public String addCategory(Model model) {
        model.addAttribute("pageTitle", "Thêm Danh mục");
        model.addAttribute("currentPage", "categories");
        model.addAttribute("category", new Category());
        return "admin/category-form";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Không tìm thấy danh mục.");
            return "redirect:/admin/categories";
        }
        model.addAttribute("pageTitle", "Sửa Danh mục");
        model.addAttribute("currentPage", "categories");
        model.addAttribute("category", categoryOpt.get());
        return "admin/category-form";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes ra) {
        String name = category.getName() != null ? category.getName().trim() : null;
        category.setName(name);
        if (name == null || name.isBlank()) {
            ra.addFlashAttribute("error", "Tên danh mục không được để trống.");
            return category.getId() == null ? "redirect:/admin/categories/add" : ("redirect:/admin/categories/edit/" + category.getId());
        }
        Category dup = categoryRepository.findByNameIgnoreCase(name);
        if (dup != null && (category.getId() == null || !java.util.Objects.equals(dup.getId(), category.getId()))) {
            ra.addFlashAttribute("error", "Tên danh mục đã tồn tại.");
            return category.getId() == null ? "redirect:/admin/categories/add" : ("redirect:/admin/categories/edit/" + category.getId());
        }
        categoryRepository.save(category);
        ra.addFlashAttribute("message", "Lưu danh mục thành công.");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Integer id, RedirectAttributes ra) {
        categoryRepository.findById(id).ifPresentOrElse(c -> {
            long cnt = productRepository.countByCategory(c);
            if (cnt > 0) {
                ra.addFlashAttribute("error", "Không thể xóa danh mục vì còn " + cnt + " sản phẩm đang thuộc danh mục này.");
            } else {
                categoryRepository.delete(c);
                ra.addFlashAttribute("message", "Đã xóa danh mục thành công.");
            }
        }, () -> ra.addFlashAttribute("error", "Không tìm thấy danh mục."));
        return "redirect:/admin/categories";
    }

    @GetMapping("/toppings")
    public String showToppings(Model model) {
        List<Topping> toppings = toppingRepository.findAll();
        model.addAttribute("pageTitle", "Topping");
        model.addAttribute("currentPage", "toppings");
        model.addAttribute("toppingList", toppings);
        return "admin/toppings";
    }

    @GetMapping("/toppings/add")
    public String addTopping(Model model) {
        model.addAttribute("pageTitle", "Thêm Topping");
        model.addAttribute("currentPage", "toppings");
        model.addAttribute("topping", new Topping());
        return "admin/topping-form";
    }

    @GetMapping("/toppings/edit/{id}")
    public String editTopping(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Optional<Topping> toppingOpt = toppingRepository.findById(id);
        if (toppingOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Không tìm thấy topping.");
            return "redirect:/admin/toppings";
        }
        model.addAttribute("pageTitle", "Sửa Topping");
        model.addAttribute("currentPage", "toppings");
        model.addAttribute("topping", toppingOpt.get());
        return "admin/topping-form";
    }

    @PostMapping("/toppings/save")
    public String saveTopping(@ModelAttribute Topping topping,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              RedirectAttributes ra) {
        try {
            String name = topping.getName() != null ? topping.getName().trim() : null;
            topping.setName(name);
            if (name == null || name.isBlank()) {
                ra.addFlashAttribute("error", "Tên topping không được để trống.");
                return topping.getId() == null ? "redirect:/admin/toppings/add" : ("redirect:/admin/toppings/edit/" + topping.getId());
            }
            Topping dup = toppingRepository.findByNameIgnoreCase(name);
            if (dup != null && (topping.getId() == null || !java.util.Objects.equals(dup.getId(), topping.getId()))) {
                ra.addFlashAttribute("error", "Tên topping đã tồn tại.");
                return topping.getId() == null ? "redirect:/admin/toppings/add" : ("redirect:/admin/toppings/edit/" + topping.getId());
            }
            if (imageFile != null && !imageFile.isEmpty()) {
                String url = storageService.uploadImage(imageFile);
                topping.setImageUrl(url);
            }
            toppingRepository.save(topping);
            ra.addFlashAttribute("message", "Lưu topping thành công.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", "Tải ảnh thất bại: " + e.getMessage());
        }
        return "redirect:/admin/toppings";
    }

    @GetMapping("/toppings/delete/{id}")
    public String deleteTopping(@PathVariable Integer id, RedirectAttributes ra) {
        toppingRepository.findById(id).ifPresent(t -> {
            t.setStatus(ToppingStatus.INACTIVE);
            toppingRepository.save(t);
        });
        ra.addFlashAttribute("message", "Đã chuyển trạng thái topping thành KHÔNG HOẠT ĐỘNG.");
        return "redirect:/admin/toppings";
    }
}
