package com.alotra.service.interaction;

import com.alotra.entity.Customer;
import com.alotra.entity.Product;
import com.alotra.entity.Wishlist;
import java.util.List;

public interface WishlistOperations {
    void addToWishlist(Customer customer, Product product);
    void removeFromWishlist(Customer customer, Integer productId);
    List<Wishlist> getCustomerWishlist(Customer customer);
    boolean isInWishlist(Customer customer, Integer productId);
}
