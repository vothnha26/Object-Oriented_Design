package com.alotra.repository;

import com.alotra.entity.Customer;
import com.alotra.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    List<OtpCode> findTop5ByCustomerAndTypeOrderByIdDesc(Customer customer, String type);
    Optional<OtpCode> findTopByCustomerAndTypeOrderByIdDesc(Customer customer, String type);
    Optional<OtpCode> findTopByCustomerAndTypeAndCodeOrderByIdDesc(Customer customer, String type, String code);
    long deleteByCustomerAndTypeAndExpiresAtBefore(Customer customer, String type, LocalDateTime threshold);
}