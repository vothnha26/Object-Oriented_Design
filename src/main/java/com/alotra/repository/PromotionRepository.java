package com.alotra.repository;

import com.alotra.entity.Promotion;
import com.alotra.entity.enums.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    List<Promotion> findByDeletedAtIsNull();
    List<Promotion> findByStatusAndDeletedAtIsNullOrderByStartDateDesc(PromotionStatus status);
    List<Promotion> findByDeletedAtIsNotNull();
}
