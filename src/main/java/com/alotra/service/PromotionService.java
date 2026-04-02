package com.alotra.service;

import com.alotra.entity.*;
import com.alotra.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PromotionService {
    private final PromotionRepository promotionRepo;

    public PromotionService(PromotionRepository promotionRepo) {
        this.promotionRepo = promotionRepo;
    }

    public List<Promotion> findAll() { return promotionRepo.findAll(); }
    public Optional<Promotion> findById(Integer id) { return promotionRepo.findById(id); }
    public Promotion save(Promotion p) { return promotionRepo.save(p); }
    public List<Promotion> findActive() { return promotionRepo.findByDeletedAtIsNull(); }

    @Transactional
    public void deleteById(Integer id) {
        promotionRepo.findById(id).ifPresent(promotionRepo::delete);
    }
}
