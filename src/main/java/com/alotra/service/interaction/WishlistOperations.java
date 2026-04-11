package com.alotra.service.interaction;

import com.alotra.entity.Customer;
import com.alotra.entity.Product;
import com.alotra.entity.Wishlist;
import java.util.List;

public interface WishlistOperations {
    List<Wishlist> findByCustomer(Integer customerId);
    void addToWishlist(Customer customer, Product product);
    void removeFromWishlist(Integer customerId, Integer productId);
}
