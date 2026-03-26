package com.alotra.repository;

import com.alotra.entity.CartItem;
import com.alotra.entity.CartItemTopping;
import com.alotra.entity.CartItemToppingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemToppingRepository extends JpaRepository<CartItemTopping, CartItemToppingId> {
    List<CartItemTopping> findByCartItem(CartItem cartItem);
}
