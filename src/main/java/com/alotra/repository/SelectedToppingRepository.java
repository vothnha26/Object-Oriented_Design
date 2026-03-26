package com.alotra.repository;

import com.alotra.entity.CartItem;
import com.alotra.entity.SelectedTopping;
import com.alotra.entity.SelectedToppingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SelectedToppingRepository extends JpaRepository<SelectedTopping, SelectedToppingId> {
    List<SelectedTopping> findByCartItem(CartItem cartItem);
}
