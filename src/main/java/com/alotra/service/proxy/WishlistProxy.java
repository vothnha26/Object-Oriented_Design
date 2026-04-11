package com.alotra.service.proxy;

import com.alotra.entity.Customer;
import com.alotra.entity.Product;
import com.alotra.entity.Wishlist;
import com.alotra.service.interaction.WishlistOperations;
import com.alotra.service.interaction.WishlistService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Primary
public class WishlistProxy implements WishlistOperations {
    private final WishlistService wishlistService;

    public WishlistProxy(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @Override
    public List<Wishlist> findByCustomer(Integer customerId) {
        return wishlistService.findByCustomer(customerId);
    }

    @Override
    public void addToWishlist(Customer customer, Product product) {
        // WHITELIST LOGIC: Chỉ cho phép sản phẩm ACTIVE và chưa bị xóa mềm
        if (product != null && product.isAvailable()) {
            wishlistService.addToWishlist(customer, product);
        } else {
            throw new IllegalStateException("Sản phẩm không có sẵn để thêm vào danh sách yêu thích.");
        }
    }

    @Override
    public void removeFromWishlist(Integer customerId, Integer productId) {
        wishlistService.removeFromWishlist(customerId, productId);
    }
}
