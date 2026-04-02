package com.alotra.service.product;

import com.alotra.dto.ProductDTO;
import com.alotra.entity.Product;
import com.alotra.entity.ProductVariant;
import com.alotra.entity.Topping;
import com.alotra.entity.Review;
import com.alotra.repository.ProductRepository;
import com.alotra.repository.ProductVariantRepository;
import com.alotra.service.interaction.ReviewService;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductFacade {
    private final ProductService productService;
    private final ToppingService toppingService;
    private final ReviewService reviewService;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    public ProductFacade(ProductService productService,
                         ToppingService toppingService,
                         ReviewService reviewService,
                         ProductRepository productRepository,
                         ProductVariantRepository variantRepository) {
        this.productService = productService;
        this.toppingService = toppingService;
        this.reviewService = reviewService;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    public List<ProductDTO> getHomeProducts() {
        return productService.findBestSellers();
    }

    public List<ProductDTO> searchProducts(Integer categoryId, String keyword) {
        return productService.listByCategoryAndSearch(categoryId, keyword);
    }

    public Product getProductDetail(Integer id) {
        if (id == null) throw new IllegalArgumentException("ID sản phẩm không được để trống");
        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));
    }

    public List<ProductVariant> getActiveVariants(Product product) {
        List<ProductVariant> variants = variantRepository.findByProduct(product);
        variants.removeIf(v -> !v.isActive());
        variants.sort(Comparator.comparing(ProductVariant::getPrice));
        return variants;
    }

    public List<Topping> getAvailableToppings() {
        return toppingService.findActive();
    }

    public List<Review> getProductReviews(Integer productId) {
        return reviewService.listByProduct(productId, null);
    }
}
