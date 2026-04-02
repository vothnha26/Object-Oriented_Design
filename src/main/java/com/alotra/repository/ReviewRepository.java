package com.alotra.repository;

import com.alotra.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    
    interface ProductRatingStats {
        Double getAvg();
        Long getCnt();
    }

    @Query("SELECT AVG(r.stars) as avg, COUNT(r) as cnt FROM Review r WHERE r.product.id = :productId")
    ProductRatingStats findStatsByProductId(@Param("productId") Integer productId);

    List<Review> findByProductIdOrderByCreatedAtDesc(Integer productId);
    
    List<Review> findByCustomerIdAndProductIdIn(Integer customerId, List<Integer> productIds);
    
    Optional<Review> findByCustomerIdAndProductIdAndOrderId(Integer customerId, Integer productId, Integer orderId);

    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    List<Review> findAllOrderByCreatedAtDesc();
}
