package com.alotra.repository;

import com.alotra.entity.Promotion;
import com.alotra.entity.enums.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    List<Promotion> findByStatus(PromotionStatus status);
    Optional<Promotion> findByCode(String code);
}
