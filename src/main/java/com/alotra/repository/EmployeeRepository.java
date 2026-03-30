package com.alotra.repository;

import com.alotra.entity.Employee;
import com.alotra.entity.enums.EmployeeRole;
import com.alotra.entity.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByPhone(String phone);

    @Query("SELECT n FROM Employee n WHERE n.deletedAt IS NULL AND " +
            "(:kw IS NULL OR LOWER(n.username) LIKE LOWER(CONCAT('%',:kw,'%')) OR LOWER(n.email) LIKE LOWER(CONCAT('%',:kw,'%')) OR LOWER(n.fullName) LIKE LOWER(CONCAT('%',:kw,'%')) OR n.phone LIKE CONCAT('%',:kw,'%')) AND " +
            "(:role IS NULL OR n.role = :role) AND " +
            "(:status IS NULL OR n.status = :status)")
    List<Employee> search(@Param("kw") String kw, @Param("role") EmployeeRole role, @Param("status") EmployeeStatus status);

    List<Employee> findByDeletedAtIsNull();
    List<Employee> findByDeletedAtIsNotNull();
}
