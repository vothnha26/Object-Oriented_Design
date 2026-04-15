package com.alotra.repository;

import com.alotra.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    
    interface ProductRatingStats {
        Double getAvg();
        Long getCnt();
    }

    @Query("SELECT AVG(r.stars) as avg, COUNT(r) as cnt FROM Review r WHERE r.productId = :productId")
    ProductRatingStats findStatsByProductId(@Param("productId") Integer productId);

    List<Review> findByProductId(Integer productId);
    
    List<Review> findByUserId(Integer userId);
}
