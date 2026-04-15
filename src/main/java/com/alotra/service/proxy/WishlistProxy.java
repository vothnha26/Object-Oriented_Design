package com.alotra.service.proxy;

import com.alotra.entity.Customer;
import com.alotra.entity.Product;
import com.alotra.entity.Wishlist;
import com.alotra.service.interaction.WishlistOperations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Primary
public class WishlistProxy implements WishlistOperations {
    private final WishlistOperations wishlistService;

    public WishlistProxy(@Qualifier("wishlistService") WishlistOperations wishlistService) {
        this.wishlistService = wishlistService;
    }

    @Override
    public void addToWishlist(Customer customer, Product product) {
        // Whitelist Logic: Chỉ cho phép thêm nếu sản phẩm đang kinh doanh và chưa bị
        // xóa
        if (product != null && product.isAvailable()) {
            wishlistService.addToWishlist(customer, product);
        } else {
            throw new IllegalStateException("Sản phẩm hiện không khả dụng để thêm vào danh sách yêu thích.");
        }
    }

    @Override
    public void removeFromWishlist(Customer customer, Integer productId) {
        wishlistService.removeFromWishlist(customer, productId);
    }

    @Override
    public List<Wishlist> getCustomerWishlist(Customer customer) {
        return wishlistService.getCustomerWishlist(customer);
    }

    @Override
    public boolean isInWishlist(Customer customer, Integer productId) {
        return wishlistService.isInWishlist(customer, productId);
    }
}
