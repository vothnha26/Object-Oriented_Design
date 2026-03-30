package com.alotra.controller.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alotra.entity.Category;
import com.alotra.entity.Product;
import com.alotra.entity.ProductSize;
import com.alotra.entity.ProductVariant;
import com.alotra.entity.enums.ProductStatus;
import com.alotra.repository.CategoryRepository;
import com.alotra.repository.OrderItemRepository;
import com.alotra.repository.ProductRepository;
import com.alotra.repository.ProductSizeRepository;
import com.alotra.repository.ProductVariantRepository;
import com.alotra.storage.ImageStorageService;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSizeRepository sizeRepository;
    private final ProductVariantRepository variantRepository;
    private final ImageStorageService storageService;
    private final OrderItemRepository orderItemRepository;

    public AdminProductController(ProductRepository productRepository,
                                  CategoryRepository categoryRepository,
                                  ProductSizeRepository sizeRepository,
                                  ProductVariantRepository variantRepository,
                                  ImageStorageService storageService,
                                  OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.sizeRepository = sizeRepository;
        this.variantRepository = variantRepository;
        this.storageService = storageService;
        this.orderItemRepository = orderItemRepository;
    }

    @GetMapping
    public String list(Model model,
                       @RequestParam(value = "kw", required = false) String kw,
                       @RequestParam(value = "categoryId", required = false) Integer categoryId,
                       @RequestParam(value = "status", required = false) Integer status) {
        String keyword = (kw != null && !kw.isBlank()) ? kw.trim() : null;
        ProductStatus statusEnum = null;
        if (status != null) {
            try {
                statusEnum = ProductStatus.fromValue(status);
            } catch (Exception ignored) { }
        }
        List<Product> items = productRepository.adminSearch(keyword, categoryId, statusEnum);
        model.addAttribute("pageTitle", "Sản phẩm");
        model.addAttribute("currentPage", "products");
        model.addAttribute("items", items);
        model.addAttribute("kw", kw);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        model.addAttribute("categories", categoryRepository.findByDeletedAtIsNull());
        return "admin/products";
    }

    @GetMapping("/{id}/variants/json")
    @ResponseBody
    public ResponseEntity<?> getVariantsJson(@PathVariable Integer id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) return ResponseEntity.notFound().build();
        List<ProductVariant> list = variantRepository.findByProductFetchingSize(productOpt.get());
        List<Map<String, Object>> data = new ArrayList<>();
        for (ProductVariant v : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", v.getId());
            m.put("size", v.getSize() != null ? v.getSize().getName() : null);
            m.put("price", v.getPrice());
            m.put("status", v.getStatus());
            data.add(m);
        }
        return ResponseEntity.ok(data);
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("pageTitle", "Thêm sản phẩm");
        model.addAttribute("currentPage", "products");
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findByDeletedAtIsNull());
        model.addAttribute("sizes", sizeRepository.findAll());
        return "admin/product-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Optional<Product> opt = productRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Không tìm thấy sản phẩm.");
            return "redirect:/admin/products";
        }
        Product p = opt.get();
        model.addAttribute("pageTitle", "Sửa sản phẩm");
        model.addAttribute("currentPage", "products");
        model.addAttribute("product", p);
        model.addAttribute("categories", categoryRepository.findByDeletedAtIsNull());
        model.addAttribute("variants", variantRepository.findByProductFetchingSize(p));
        model.addAttribute("sizes", sizeRepository.findAll());
        return "admin/product-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Product product,
                       @RequestParam("categoryId") Integer categoryId,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       @RequestParam(value = "variantSizeId", required = false) List<Integer> variantSizeIds,
                       @RequestParam(value = "variantPrice", required = false) List<BigDecimal> variantPrices,
                       @RequestParam(value = "variantStatus", required = false) List<Integer> variantStatuses,
                       RedirectAttributes ra) {
        String name = product.getName() != null ? product.getName().trim() : null;
        product.setName(name);
        if (name == null || name.isBlank()) {
            ra.addFlashAttribute("error", "Tên sản phẩm không được để trống.");
            return product.getId() == null ? "redirect:/admin/products/add" : ("redirect:/admin/products/edit/" + product.getId());
        }
        var dup = productRepository.findByNameIgnoreCaseAndDeletedAtIsNull(name);
        if (dup != null && (product.getId() == null || !java.util.Objects.equals(dup.getId(), product.getId()))) {
            ra.addFlashAttribute("error", "Tên sản phẩm đã tồn tại.");
            return product.getId() == null ? "redirect:/admin/products/add" : ("redirect:/admin/products/edit/" + product.getId());
        }
        Category cat = new Category();
        cat.setId(categoryId);
        product.setCategory(cat);
        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            String url = storageService.uploadImage(imageFile);
            product.setImageUrl(url);
        }
        product = productRepository.save(product);

        if (variantSizeIds != null && !variantSizeIds.isEmpty()) {
            int n = variantSizeIds.size();
            int pn = variantPrices != null ? variantPrices.size() : 0;
            int sn = variantStatuses != null ? variantStatuses.size() : 0;
            int limit = Math.min(n, Math.min(pn, Math.max(sn, n)));
            Set<Integer> seenSizeIds = new HashSet<>();
            for (int i = 0; i < limit; i++) {
                Integer sizeId = variantSizeIds.get(i);
                BigDecimal price = (variantPrices != null && i < variantPrices.size()) ? variantPrices.get(i) : null;
                Integer statusVal = (variantStatuses != null && i < variantStatuses.size()) ? variantStatuses.get(i) : 1;
                if (sizeId == null || price == null) continue;
                if (price.signum() < 0) continue;
                if (!seenSizeIds.add(sizeId)) continue;
                ProductVariant v = new ProductVariant();
                v.setProduct(product);
                ProductSize sz = new ProductSize();
                sz.setId(sizeId);
                v.setSize(sz);
                v.setPrice(price);
                v.setStatus(ProductStatus.fromValue(statusVal != null ? statusVal : 1));
                variantRepository.save(v);
            }
        }
        ra.addFlashAttribute("message", "Lưu sản phẩm thành công.");
        return "redirect:/admin/products";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        Optional<Product> opt = productRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Không tìm thấy sản phẩm.");
            return "redirect:/admin/products";
        }
        Product p = opt.get();

        long usedInOrders = 0L;
        try {
            usedInOrders = orderItemRepository.countByVariant_Product_Id(p.getId());
        } catch (Exception ignored) { }
        if (usedInOrders > 0) {
            ra.addFlashAttribute("error", "Không thể xóa sản phẩm vì đã phát sinh đơn hàng.");
            return "redirect:/admin/products";
        }
        p.setDeletedAt(LocalDateTime.now());
        p.setStatus(ProductStatus.INACTIVE);
        productRepository.save(p);
        ra.addFlashAttribute("message", "Đã chuyển sản phẩm vào thùng rác.");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/variants")
    public String addVariant(@PathVariable Integer id,
                             @RequestParam("sizeId") Integer sizeId,
                             @RequestParam("price") BigDecimal price,
                             @RequestParam(value = "status", defaultValue = "1") Integer status,
                             RedirectAttributes ra) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Không tìm thấy sản phẩm.");
            return "redirect:/admin/products";
        }
        Product p = productOpt.get();
        ProductSize size = new ProductSize();
        size.setId(sizeId);
        ProductVariant v = new ProductVariant();
        v.setProduct(p);
        v.setSize(size);
        v.setPrice(price);
        v.setStatus(ProductStatus.fromValue(status));
        variantRepository.save(v);
        return "redirect:/admin/products/edit/" + id;
    }

    @GetMapping("/{id}/variants/{variantId}/delete")
    public String deleteVariant(@PathVariable Integer id, @PathVariable Integer variantId, RedirectAttributes ra) {
        try {
            variantRepository.deleteById(variantId);
            ra.addFlashAttribute("message", "Đã xóa biến thể.");
        } catch (DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error", "Không thể xóa biến thể vì đang được tham chiếu trong đơn hàng/giỏ hàng.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Không thể xóa biến thể: " + ex.getMessage());
        }
        return "redirect:/admin/products/edit/" + id;
    }
}
