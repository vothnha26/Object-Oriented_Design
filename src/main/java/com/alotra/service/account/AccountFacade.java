package com.alotra.service.account;

import com.alotra.entity.Customer;
import com.alotra.entity.enums.CustomerStatus;
import com.alotra.factory.UserFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AccountFacade {
    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;
    private final UserFactory userFactory;

    public AccountFacade(CustomerService customerService, PasswordEncoder passwordEncoder, UserFactory userFactory) {
        this.customerService = customerService;
        this.passwordEncoder = passwordEncoder;
        this.userFactory = userFactory;
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

    public void changePassword(String username, String newPassword) {
        Customer customer = customerService.findByUsername(username);
        if (customer == null) throw new IllegalArgumentException("Khách hàng không tồn tại");
        
        customer.setPasswordHash(passwordEncoder.encode(newPassword));
        customerService.save(customer);
    }

    public void registerCustomer(String username, String email, String fullName, String phone, String password) {
        if (customerService.findByUsername(username) != null) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (customerService.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        // Sử dụng Factory để tạo User thay vì new trực tiếp
        Customer customer = userFactory.createPendingCustomer(username, email, fullName, phone, password);
        
        customerService.save(customer);
    }
}
