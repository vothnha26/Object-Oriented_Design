package com.alotra.repository;

import com.alotra.entity.OrderedTopping;
import com.alotra.entity.OrderedToppingId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderedToppingRepository extends JpaRepository<OrderedTopping, OrderedToppingId> {
}
