package com.alotra.controller.api;

import com.alotra.entity.Promotion;
import com.alotra.repository.PromotionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/promotions")
public class PromotionApiController {

    private final PromotionRepository promotionRepo;

    public PromotionApiController(PromotionRepository promotionRepo) {
        this.promotionRepo = promotionRepo;
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkPromo(@RequestParam String code) {
        Map<String, Object> response = new HashMap<>();
        
        var promoOpt = promotionRepo.findByCode(code);
        if (promoOpt.isPresent()) {
            Promotion p = promoOpt.get();
            if (p.isActive()) {
                response.put("valid", true);
                response.put("description", p.getName());
                return ResponseEntity.ok(response);
            } else {
                response.put("valid", false);
                response.put("message", "Mã đã hết hạn hoặc hết lượt dùng.");
            }
        } else {
            response.put("valid", false);
            response.put("message", "Mã giảm giá không tồn tại.");
        }
        
        return ResponseEntity.ok(response);
    }
}
