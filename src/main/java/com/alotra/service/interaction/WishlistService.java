package com.alotra.service.interaction;

import com.alotra.entity.Customer;
import com.alotra.entity.Product;
import com.alotra.entity.Wishlist;
import com.alotra.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service("wishlistService")
public class WishlistService implements WishlistOperations {
    private final WishlistRepository wishlistRepository;

    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    @Override
    @Transactional
    public void addToWishlist(Customer customer, Product product) {
        if (!isInWishlist(customer, product.getId())) {
            Wishlist wishlist = new Wishlist();
            wishlist.setCustomer(customer);
            wishlist.setProduct(product);
            wishlist.setAddedAt(LocalDateTime.now());
            wishlistRepository.save(wishlist);
        }
    }

    @Override
    @Transactional
    public void removeFromWishlist(Customer customer, Integer productId) {
        wishlistRepository.deleteByCustomerIdAndProductId(customer.getId(), productId);
    }

    @Override
    public List<Wishlist> getCustomerWishlist(Customer customer) {
        return wishlistRepository.findByCustomerIdOrderByAddedAtDesc(customer.getId());
    }

    @Override
    public boolean isInWishlist(Customer customer, Integer productId) {
        return wishlistRepository.existsByCustomerIdAndProductId(customer.getId(), productId);
    }
}
