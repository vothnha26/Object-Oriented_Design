package com.alotra.service.command;

import com.alotra.entity.Product;
import com.alotra.entity.enums.ProductStatus;
import com.alotra.repository.ProductRepository;

public class SoftDeleteProductCommand implements AdminCommand {
    private final ProductRepository productRepository;
    private final Integer productId;
    
    private ProductStatus previousStatus;

    public SoftDeleteProductCommand(ProductRepository productRepository, Integer productId) {
        this.productRepository = productRepository;
        this.productId = productId;
    }

    @Override
    public void execute() {
        Product p = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        this.previousStatus = p.getStatus();
        
        p.setStatus(ProductStatus.INACTIVE);
        productRepository.save(p);
    }

    @Override
    public void undo() {
        Product p = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        p.setStatus(previousStatus);
        productRepository.save(p);
    }

    @Override
    public String getDescription() {
        return "Thay đổi trạng thái sản phẩm #" + productId + " thành KHÔNG HOẠT ĐỘNG";
    }
}
