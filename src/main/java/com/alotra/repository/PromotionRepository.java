package com.alotra.repository;

import com.alotra.entity.Promotion;
import com.alotra.entity.enums.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    List<Promotion> findTop8ByStatusOrderByStartDateDesc(PromotionStatus status);
    List<Promotion> findTop8ByStatusAndDeletedAtIsNullOrderByStartDateDesc(PromotionStatus status);

    @Modifying
    @Query("UPDATE Promotion s SET s.views = COALESCE(s.views,0) + 1 WHERE s.id = :id")
    int incrementViews(@Param("id") Integer id);

    List<Promotion> findByDeletedAtIsNull();
    List<Promotion> findByDeletedAtIsNotNull();
}
