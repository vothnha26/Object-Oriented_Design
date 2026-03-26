package com.alotra.repository;

import com.alotra.entity.Customer;
import com.alotra.entity.enums.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByPhone(String phone);

    @Query("SELECT k FROM Customer k WHERE " +
            "(:kw IS NULL OR LOWER(k.username) LIKE LOWER(CONCAT('%',:kw,'%')) OR LOWER(k.email) LIKE LOWER(CONCAT('%',:kw,'%')) OR LOWER(k.fullName) LIKE LOWER(CONCAT('%',:kw,'%')) OR k.phone LIKE CONCAT('%',:kw,'%')) AND " +
            "(:status IS NULL OR k.status = :status)")
    List<Customer> search(@Param("kw") String kw, @Param("status") CustomerStatus status);
}
