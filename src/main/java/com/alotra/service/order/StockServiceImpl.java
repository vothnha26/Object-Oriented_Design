package com.alotra.service.order;

import com.alotra.dto.CartItemDTO;
import com.alotra.entity.ProductVariant;
import com.alotra.repository.ProductRepository;
import com.alotra.repository.ProductSizeRepository;
import com.alotra.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ProductSizeRepository sizeRepository;

    @Override
    public void validateStock(List<CartItemDTO> cartItems) {
        for (CartItemDTO item : cartItems) {
            ProductVariant v = variantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Biến thể không tồn tại"));

            if (!v.isActive()) {
                throw new IllegalStateException("Sản phẩm hiện không khả dụng");
            }
        }
    }
}
