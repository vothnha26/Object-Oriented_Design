package com.alotra.service;

import com.alotra.dto.ProductDTO;
import com.alotra.repository.ProductRepository;
import com.alotra.repository.AppliedPromotionRepository;
import com.alotra.discount.DiscountStrategy;
import com.alotra.discount.PercentDiscountStrategy;
import com.alotra.discount.NoDiscountStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AppliedPromotionRepository appliedPromotionRepository;

    public List<ProductDTO> findBestSellers() {
        return productRepository.findBestSellersNative().stream()
                .map(row -> {
                    BigDecimal minBase = row.getPrice();
                    Integer percent = row.getId() != null ? appliedPromotionRepository.findActiveMaxDiscountPercentForProduct(row.getId()) : null;
                    
                    // Use DiscountStrategy instead of applyPercent
                    DiscountStrategy discount = (percent != null && percent > 0) 
                        ? new PercentDiscountStrategy(percent) 
                        : new NoDiscountStrategy();
                    BigDecimal finalPrice = discount.apply(minBase);
                    
                    ProductDTO dto = new ProductDTO(
                            row.getId(),
                            row.getName(),
                            (row.getImageUrl() != null && !row.getImageUrl().isBlank()) ? row.getImageUrl() : "/images/placeholder.png",
                            finalPrice
                    );
                    dto.setOriginalPrice(minBase);
                    dto.setDiscountPercent(percent);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ProductDTO> listByCategory(Integer categoryId) {
        return productRepository.findListByCategoryNative(categoryId).stream()
                .map(row -> {
                    BigDecimal minBase = row.getPrice();
                    Integer percent = row.getId() != null ? appliedPromotionRepository.findActiveMaxDiscountPercentForProduct(row.getId()) : null;
                    
                    // Use DiscountStrategy instead of applyPercent
                    DiscountStrategy discount = (percent != null && percent > 0) 
                        ? new PercentDiscountStrategy(percent) 
                        : new NoDiscountStrategy();
                    BigDecimal finalPrice = discount.apply(minBase);
                    
                    ProductDTO dto = new ProductDTO(
                            row.getId(),
                            row.getName(),
                            (row.getImageUrl() != null && !row.getImageUrl().isBlank()) ? row.getImageUrl() : "/images/placeholder.png",
                            finalPrice
                    );
                    dto.setOriginalPrice(minBase);
                    dto.setDiscountPercent(percent);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ProductDTO> search(String keyword) {
        if (keyword == null) keyword = "";
        String kw = keyword.trim();
        return productRepository.searchByKeywordNative(kw).stream()
                .map(row -> {
                    BigDecimal minBase = row.getPrice();
                    Integer percent = row.getId() != null ? appliedPromotionRepository.findActiveMaxDiscountPercentForProduct(row.getId()) : null;
                    
                    // Use DiscountStrategy instead of applyPercent
                    DiscountStrategy discount = (percent != null && percent > 0) 
                        ? new PercentDiscountStrategy(percent) 
                        : new NoDiscountStrategy();
                    BigDecimal finalPrice = discount.apply(minBase);
                    
                    ProductDTO dto = new ProductDTO(
                            row.getId(),
                            row.getName(),
                            (row.getImageUrl() != null && !row.getImageUrl().isBlank()) ? row.getImageUrl() : "/images/placeholder.png",
                            finalPrice
                    );
                    dto.setOriginalPrice(minBase);
                    dto.setDiscountPercent(percent);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ProductDTO> listByCategoryAndSearch(Integer categoryId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listByCategory(categoryId);
        }
        
        String kw = keyword.trim();
        return productRepository.searchByCategoryAndKeywordNative(categoryId, kw).stream()
                .map(row -> {
                    BigDecimal minBase = row.getPrice();
                    Integer percent = row.getId() != null ? appliedPromotionRepository.findActiveMaxDiscountPercentForProduct(row.getId()) : null;
                    
                    // Use DiscountStrategy instead of applyPercent
                    DiscountStrategy discount = (percent != null && percent > 0) 
                        ? new PercentDiscountStrategy(percent) 
                        : new NoDiscountStrategy();
                    BigDecimal finalPrice = discount.apply(minBase);
                    
                    ProductDTO dto = new ProductDTO(
                            row.getId(),
                            row.getName(),
                            (row.getImageUrl() != null && !row.getImageUrl().isBlank()) ? row.getImageUrl() : "/images/placeholder.png",
                            finalPrice
                    );
                    dto.setOriginalPrice(minBase);
                    dto.setDiscountPercent(percent);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}