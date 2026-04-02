package com.alotra.repository;

import com.alotra.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    List<Wishlist> findByCustomerId(Integer customerId);
    boolean existsByCustomerIdAndProductId(Integer customerId, Integer productId);
    void deleteByCustomerIdAndProductId(Integer customerId, Integer productId);
}
