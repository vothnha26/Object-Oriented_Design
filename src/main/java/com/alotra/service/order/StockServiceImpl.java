package com.alotra.service.order;

import com.alotra.dto.CartItemDTO;
import com.alotra.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockServiceImpl implements StockService {
    private final ProductVariantRepository variantRepository;

    public StockServiceImpl(ProductVariantRepository variantRepository) {
        this.variantRepository = variantRepository;
    }

    @Override
    public void validateStock(List<CartItemDTO> items) {
        // Logic kiểm tra tính khả dụng (Status) dựa trên variants
        for (CartItemDTO item : items) {
            variantRepository.findById(item.getVariantId())
                    .ifPresent(v -> {
                        if (!v.isActive()) {
                            throw new IllegalStateException("Biến thể " + v.getSize().getName() + " của sản phẩm " + v.getProduct().getName() + " hiện không hỗ trợ kinh doanh.");
                        }
                    });
        }
    }
}
