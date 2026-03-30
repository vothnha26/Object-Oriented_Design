package com.alotra.service.command;

import com.alotra.entity.Product;
import com.alotra.entity.enums.ProductStatus;
import com.alotra.repository.ProductRepository;
import java.time.LocalDateTime;

public class SoftDeleteProductCommand implements AdminCommand {
    private final ProductRepository productRepository;
    private final Integer productId;
    
    private LocalDateTime previousDeletedAt;
    private ProductStatus previousStatus;

    public SoftDeleteProductCommand(ProductRepository productRepository, Integer productId) {
        this.productRepository = productRepository;
        this.productId = productId;
    }

    @Override
    public void execute() {
        Product p = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        this.previousDeletedAt = p.getDeletedAt();
        this.previousStatus = p.getStatus();
        
        p.setDeletedAt(LocalDateTime.now());
        p.setStatus(ProductStatus.INACTIVE);
        productRepository.save(p);
    }

    @Override
    public void undo() {
        Product p = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        p.setDeletedAt(previousDeletedAt);
        p.setStatus(previousStatus);
        productRepository.save(p);
    }

    @Override
    public String getDescription() {
        return "Xóa mềm sản phẩm #" + productId;
    }
}
