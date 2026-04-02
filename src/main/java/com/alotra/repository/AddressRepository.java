package com.alotra.repository;

import com.alotra.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    List<Address> findByCustomerId(Integer customerId);
    List<Address> findByCustomerIdAndIsDefaultTrue(Integer customerId);
}
