package com.alotra.controller.vendor;

import com.alotra.entity.KhuyenMaiSanPham;
import com.alotra.entity.SuKienKhuyenMai;
import com.alotra.entity.Product;
import com.alotra.service.PromotionService;
import com.alotra.service.CloudinaryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/vendor/promotions")
public class VendorPromotionController {
    private final PromotionService promotionService;
    private final CloudinaryService cloudinaryService;

    public VendorPromotionController(PromotionService promotionService, CloudinaryService cloudinaryService) {
        this.promotionService = promotionService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "Sự kiện khuyến mãi");
        model.addAttribute("currentPage", "vendor-promotions");
        model.addAttribute("items", promotionService.findActive());
        return "vendor/promotion-list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", "Thêm sự kiện");
        model.addAttribute("currentPage", "vendor-promotions");
        model.addAttribute("item", new SuKienKhuyenMai());
        return "vendor/promotion-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        SuKienKhuyenMai item = promotionService.findById(id).orElseThrow();
        model.addAttribute("pageTitle", "Sửa sự kiện");
        model.addAttribute("currentPage", "vendor-promotions");
        model.addAttribute("item", item);
        return "vendor/promotion-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("item") @Valid SuKienKhuyenMai item,
                       BindingResult result,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       Model model,
                       RedirectAttributes ra) {
        LocalDate s = item.getStartDate();
        LocalDate e = item.getEndDate();
        if (s != null && e != null && e.isBefore(s)) {
            result.rejectValue("endDate", "date.invalid", "Ngày kết thúc phải >= ngày bắt đầu");
        }
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", item.getId() == null ? "Thêm sự kiện" : "Sửa sự kiện");
            model.addAttribute("currentPage", "vendor-promotions");
            return "vendor/promotion-form";
        }
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String url = cloudinaryService.uploadFile(imageFile);
                item.setImageUrl(url);
            } else if (item.getId() != null) {
                promotionService.findById(item.getId()).ifPresent(old -> item.setImageUrl(old.getImageUrl()));
            }
        } catch (Exception ex) {
            result.rejectValue("imageUrl", "upload.error", "Lỗi tải ảnh: " + ex.getMessage());
            model.addAttribute("pageTitle", item.getId() == null ? "Thêm sự kiện" : "Sửa sự kiện");
            model.addAttribute("currentPage", "vendor-promotions");
            return "vendor/promotion-form";
        }
        boolean isNew = (item.getId() == null);
        promotionService.save(item);
        ra.addFlashAttribute("message", isNew ? "Đã thêm sự kiện khuyến mãi." : "Đã cập nhật sự kiện khuyến mãi.");
        return "redirect:/vendor/promotions";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        var promoOpt = promotionService.findById(id);
        if (promoOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Không tìm thấy sự kiện khuyến mãi.");
            return "redirect:/vendor/promotions";
        }
        SuKienKhuyenMai p = promoOpt.get();
        if (promotionService.listAssignments(id).size() > 0) {
            ra.addFlashAttribute("error", "Sự kiện đang áp dụng sản phẩm, không thể xóa. Vui lòng gỡ áp dụng trước.");
            return "redirect:/vendor/promotions";
        }
        p.setDeletedAt(java.time.LocalDateTime.now());
        promotionService.save(p);
        ra.addFlashAttribute("message", "Đã chuyển sự kiện vào thùng rác.");
        return "redirect:/vendor/promotions";
    }

    @GetMapping("/{id}/products")
    public String manageProducts(@PathVariable Integer id, Model model) {
        SuKienKhuyenMai promo = promotionService.findById(id).orElseThrow();
        List<KhuyenMaiSanPham> assigned = promotionService.listAssignments(id);
        List<Product> unassigned = promotionService.listUnassignedProducts(id);
        model.addAttribute("pageTitle", "Áp sản phẩm - " + promo.getName());
        model.addAttribute("currentPage", "vendor-promotions");
        model.addAttribute("promo", promo);
        model.addAttribute("assigned", assigned);
        model.addAttribute("unassigned", unassigned);
        return "vendor/promotion-products";
    }

    @PostMapping("/{id}/products")
    public String addProduct(@PathVariable Integer id,
                             @RequestParam("productId") Integer productId,
                             @RequestParam("percent") Integer percent,
                             RedirectAttributes ra) {
        try {
            promotionService.assignProduct(id, productId, percent);
            ra.addFlashAttribute("message", "Đã áp dụng sản phẩm vào sự kiện.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/vendor/promotions/" + id + "/products";
    }

    @GetMapping("/{id}/products/{productId}/remove")
    public String removeProduct(@PathVariable Integer id, @PathVariable Integer productId, RedirectAttributes ra) {
        promotionService.unassignProduct(id, productId);
        ra.addFlashAttribute("message", "Đã gỡ sản phẩm khỏi sự kiện.");
        return "redirect:/vendor/promotions/" + id + "/products";
    }
}
