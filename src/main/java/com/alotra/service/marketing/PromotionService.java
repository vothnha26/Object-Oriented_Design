package com.alotra.service.marketing;

import com.alotra.entity.*;
import com.alotra.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class PromotionService {
    private final PromotionRepository promotionRepo;

    public PromotionService(PromotionRepository promotionRepo) {
        this.promotionRepo = promotionRepo;
    }

    public List<Promotion> findAll() { return promotionRepo.findAll(); }
    public Optional<Promotion> findById(Integer id) { return promotionRepo.findById(id); }
    public Optional<Promotion> findByCode(String code) { return promotionRepo.findByCode(code); }
    public Promotion save(Promotion p) { return promotionRepo.save(p); }

    public List<Promotion> getActivePromotions() {
        return promotionRepo.findActivePromotions(LocalDate.now());
    }

    @Transactional
    public void deleteById(Integer id) {
        promotionRepo.findById(id).ifPresent(promotionRepo::delete);
    }
}
