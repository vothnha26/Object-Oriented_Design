package com.alotra.repository;

import com.alotra.entity.OrderItemTopping;
import com.alotra.entity.OrderItemToppingId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemToppingRepository extends JpaRepository<OrderItemTopping, OrderItemToppingId> {
}
