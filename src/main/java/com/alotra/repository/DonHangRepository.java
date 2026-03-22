package com.alotra.repository;

import com.alotra.entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DonHangRepository extends JpaRepository<DonHang, Integer> {
    // Shipper methods
    long countByEmployeeIdAndStatus(Integer employeeId, String status);
    
    long countByEmployeeIdAndStatusAndCreatedAtBetween(Integer employeeId, String status, LocalDateTime start, LocalDateTime end);
    
    long countByEmployeeIdAndStatusIn(Integer employeeId, List<String> statuses);
    
    long countByEmployeeIdAndStatusAndCreatedAtAfter(Integer employeeId, String status, LocalDateTime after);
    
    List<DonHang> findByEmployeeIdAndStatus(Integer employeeId, String status);
    
    List<DonHang> findByEmployeeIdAndStatusIn(Integer employeeId, List<String> statuses);
    
    List<DonHang> findByEmployeeIdAndStatusAndCreatedAtBetween(Integer employeeId, String status, LocalDateTime start, LocalDateTime end);
}
