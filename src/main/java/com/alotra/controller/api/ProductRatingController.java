package com.alotra.controller.api;

import com.alotra.repository.ReviewRepository;
import com.alotra.service.interaction.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductRatingController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/{id}/rating")
    public Map<String, Object> getProductRating(@PathVariable Integer id) {
        var stats = reviewService.statsForProduct(id);
        return Map.of(
            "average", stats.getAvg() != null ? stats.getAvg() : 0.0,
            "count", stats.getCnt()
        );
    }
}
