package com.alotra.service.interaction;

import com.alotra.entity.Customer;
import com.alotra.entity.Product;
import com.alotra.entity.Wishlist;
import com.alotra.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class WishlistService {
    private final WishlistRepository wishlistRepository;

    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    public List<Wishlist> findByCustomer(Integer customerId) {
        return wishlistRepository.findByCustomerId(customerId);
    }

    public void addToWishlist(Customer customer, Product product) {
        if (!wishlistRepository.existsByCustomerIdAndProductId(customer.getId(), product.getId())) {
            Wishlist wishlist = new Wishlist();
            wishlist.setCustomer(customer);
            wishlist.setProduct(product);
            wishlistRepository.save(wishlist);
        }
    }

    public void removeFromWishlist(Integer customerId, Integer productId) {
        wishlistRepository.deleteByCustomerIdAndProductId(customerId, productId);
    }
}
