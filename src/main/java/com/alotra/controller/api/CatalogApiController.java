package com.alotra.controller.api;

import com.alotra.dto.ProductDTO;
import com.alotra.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogApiController {
    private final ProductService productService;

    public CatalogApiController(ProductService productService) {
        this.productService = productService;
    }

    // GET /api/catalog/products?categoryId=123&search=keyword | null for all
    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> list(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(productService.listByCategoryAndSearch(categoryId, search));
    }
}
