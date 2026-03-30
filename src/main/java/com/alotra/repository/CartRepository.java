package com.alotra.repository;

import com.alotra.entity.enums.CartStatus;
import com.alotra.entity.Cart;
import com.alotra.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findFirstByCustomerAndStatus(Customer customer, CartStatus status);
}
