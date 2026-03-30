package com.alotra.security;

import com.alotra.entity.Customer;
import com.alotra.entity.Employee;
import com.alotra.entity.enums.CustomerStatus;
import com.alotra.repository.CustomerRepository;
import com.alotra.repository.EmployeeRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
@Primary
public class CompositeUserDetailsService implements UserDetailsService {
    private final EmployeeRepository employeeRepo;
    private final CustomerRepository customerRepo;

    public CompositeUserDetailsService(EmployeeRepository employeeRepo, CustomerRepository customerRepo) {
        this.employeeRepo = employeeRepo;
        this.customerRepo = customerRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String u = username != null ? username.trim() : "";
        
        // 0) Special bootstrap admin: "boss"
        if ("boss".equalsIgnoreCase(u) || "boss@alotra.com".equalsIgnoreCase(u)) {
            Customer ghost = new Customer();
            ghost.setUsername("boss");
            ghost.setEmail("boss@alotra.com");
            ghost.setFullName("AloTra Boss");
            ghost.setStatus(CustomerStatus.ACTIVE);
            String hash = new BCryptPasswordEncoder().encode("123");
            ghost.setPasswordHash(hash);
            return new CustomerUserDetails(ghost);
        }
        
        // 1) Try staff first (Employee)
        Employee employee = employeeRepo.findByUsername(u).orElse(null);
        if (employee == null) employee = employeeRepo.findByEmail(u).orElse(null);
        if (employee != null) {
            return new EmployeeUserDetails(employee);
        }
        
        // 2) Fallback to customers (Customer)
        Customer customer = customerRepo.findByUsername(u).orElse(null);
        if (customer == null) customer = customerRepo.findByEmail(u).orElse(null);
        if (customer != null) {
            return new CustomerUserDetails(customer);
        }
        
        throw new UsernameNotFoundException("Không tìm thấy tài khoản: " + username);
    }
}