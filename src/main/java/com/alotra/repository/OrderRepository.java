package com.alotra.repository;

import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.approvedBy.id = :employeeId AND o.status = :status")
    long countByEmployeeIdAndStatus(@Param("employeeId") Integer employeeId, @Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.approvedBy.id = :employeeId AND o.status = :status AND o.createdAt BETWEEN :start AND :end")
    long countByEmployeeIdAndStatusAndCreatedAtBetween(@Param("employeeId") Integer employeeId, @Param("status") OrderStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.approvedBy.id = :employeeId AND o.status IN :statuses")
    long countByEmployeeIdAndStatusIn(@Param("employeeId") Integer employeeId, @Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.approvedBy.id = :employeeId AND o.status = :status AND o.createdAt > :after")
    long countByEmployeeIdAndStatusAndCreatedAtAfter(@Param("employeeId") Integer employeeId, @Param("status") OrderStatus status, @Param("after") LocalDateTime after);

    @Query("SELECT o FROM Order o WHERE o.approvedBy.id = :employeeId AND o.status = :status")
    List<Order> findByEmployeeIdAndStatus(@Param("employeeId") Integer employeeId, @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.approvedBy.id = :employeeId AND o.status IN :statuses")
    List<Order> findByEmployeeIdAndStatusIn(@Param("employeeId") Integer employeeId, @Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.approvedBy.id = :employeeId AND o.status = :status AND o.createdAt BETWEEN :start AND :end")
    List<Order> findByEmployeeIdAndStatusAndCreatedAtBetween(@Param("employeeId") Integer employeeId, @Param("status") OrderStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId")
    List<Order> findByCustomerId(@Param("customerId") Integer customerId);

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.status = :status")
    List<Order> findByCustomerIdAndStatus(@Param("customerId") Integer customerId, @Param("status") OrderStatus status);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
