package com.alotra.repository;

import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    long countByEmployeeIdAndStatus(Integer employeeId, OrderStatus status);
    long countByEmployeeIdAndStatusAndCreatedAtBetween(Integer employeeId, OrderStatus status, LocalDateTime start, LocalDateTime end);
    long countByEmployeeIdAndStatusIn(Integer employeeId, List<OrderStatus> statuses);
    long countByEmployeeIdAndStatusAndCreatedAtAfter(Integer employeeId, OrderStatus status, LocalDateTime after);
    List<Order> findByEmployeeIdAndStatus(Integer employeeId, OrderStatus status);
    List<Order> findByEmployeeIdAndStatusIn(Integer employeeId, List<OrderStatus> statuses);
    List<Order> findByEmployeeIdAndStatusAndCreatedAtBetween(Integer employeeId, OrderStatus status, LocalDateTime start, LocalDateTime end);
    List<Order> findByCustomerId(Integer customerId);
    List<Order> findByCustomerIdAndStatus(Integer customerId, OrderStatus status);
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
