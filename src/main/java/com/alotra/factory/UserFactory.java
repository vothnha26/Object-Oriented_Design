package com.alotra.factory;

import com.alotra.entity.Customer;
import com.alotra.entity.Employee;
import com.alotra.entity.enums.CustomerStatus;
import com.alotra.entity.enums.EmployeeRole;
import com.alotra.entity.enums.EmployeeStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {
    private final PasswordEncoder encoder;

    public UserFactory(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public Customer createCustomer(String username,
                                   String email,
                                   String fullName,
                                   String phone,
                                   String plainPassword,
                                   CustomerStatus status) {
        validateCommon(username, email, plainPassword);
        Customer customer = new Customer();
        customer.setUsername(username.trim());
        customer.setEmail(email.trim());
        customer.setFullName(fullName == null ? null : fullName.trim());
        customer.setPhone(phone == null || phone.isBlank() ? null : phone.trim());
        customer.setStatus(status == null ? CustomerStatus.ACTIVE : status);
        customer.setPasswordHash(encoder.encode(plainPassword));
        return customer;
    }

    public Customer createAdmin(String username, String email, String plainPassword, String phone) {
        return createCustomer(username, email, "AloTra Administrator", phone, plainPassword, CustomerStatus.ACTIVE);
    }

    public Employee createEmployee(String username,
                                   String email,
                                   String fullName,
                                   String phone,
                                   EmployeeRole role,
                                   EmployeeStatus status,
                                   String plainPassword) {
        validateCommon(username, email, plainPassword);
        Employee employee = new Employee();
        employee.setUsername(username.trim());
        employee.setEmail(email.trim());
        employee.setFullName(fullName == null ? null : fullName.trim());
        employee.setPhone(phone == null || phone.isBlank() ? null : phone.trim());
        employee.setRole(role == null ? EmployeeRole.STAFF : role);
        employee.setStatus(status == null ? EmployeeStatus.ACTIVE : status);
        employee.setPasswordHash(encoder.encode(plainPassword));
        return employee;
    }

    private void validateCommon(String username, String email, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username không được trống");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email không được trống");
        }
        if (password == null || password.length() < 3) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 3 ký tự");
        }
    }
}