package com.alotra.repository;

import com.alotra.entity.Cart;
import com.alotra.entity.CartItem;
import com.alotra.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByCartAndVariant(Cart cart, ProductVariant variant);
}
