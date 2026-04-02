package com.alotra.service.account;

import com.alotra.entity.Customer;
import com.alotra.entity.enums.CustomerStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AccountFacade {
    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;

    public AccountFacade(CustomerService customerService, PasswordEncoder passwordEncoder) {
        this.customerService = customerService;
        this.passwordEncoder = passwordEncoder;
    }

    public Customer findByUsername(String username) {
        return customerService.findByUsername(username);
    }

    public void updateProfile(String username, String fullName, String email, String phone) {
        Customer customer = customerService.findByUsername(username);
        if (customer == null) throw new IllegalArgumentException("Khách hàng không tồn tại");
        
        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customerService.save(customer);
    }

    public void registerCustomer(String username, String email, String fullName, String phone, String password) {
        if (customerService.findByUsername(username) != null) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (customerService.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setEmail(email);
        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setPasswordHash(passwordEncoder.encode(password));
        customer.setStatus(CustomerStatus.ACTIVE);
        
        customerService.save(customer);
    }
}
