package com.alotra.repository;

import com.alotra.entity.OrderedTopping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderedToppingRepository extends JpaRepository<OrderedTopping, Integer> {
}
