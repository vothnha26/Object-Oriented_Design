package com.alotra.repository;

import com.alotra.entity.Product;
import com.alotra.entity.AppliedPromotion;
import com.alotra.entity.AppliedPromotionId;
import com.alotra.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppliedPromotionRepository extends JpaRepository<AppliedPromotion, AppliedPromotionId> {
    List<AppliedPromotion> findByPromotion(Promotion promotion);
    long deleteByPromotion(Promotion promotion);
    boolean existsByPromotionAndProduct(Promotion promotion, Product product);
    boolean existsByProduct(Product product);

    @Query("SELECT MAX(pp.discountPercent) FROM AppliedPromotion pp " +
            "JOIN pp.promotion p " +
            "WHERE pp.product.id = :productId AND p.status = 'ACTIVE' " +
            "AND CURRENT_DATE BETWEEN p.startDate AND p.endDate")
    Integer findActiveMaxDiscountPercentForProduct(@Param("productId") Integer productId);
}
