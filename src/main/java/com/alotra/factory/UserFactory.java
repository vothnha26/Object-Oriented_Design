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
    private final PasswordEncoder passwordEncoder;

    public UserFactory(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Tạo Customer ở trạng thái PENDING (đợi xác thực OTP)
     */
    public Customer createPendingCustomer(String username, String email, String fullName, String phone, String password) {
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setEmail(email);
        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setPasswordHash(passwordEncoder.encode(password));
        customer.setStatus(CustomerStatus.PENDING);
        return customer;
    }

    /**
     * Tạo Employee (Nhân viên) với vai trò và trạng thái mặc định
     */
    public Employee createEmployee(String username, String email, String fullName, String phone, String password, EmployeeRole role) {
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setEmail(email);
        employee.setFullName(fullName);
        employee.setPhone(phone);
        employee.setPasswordHash(passwordEncoder.encode(password));
        employee.setRole(role != null ? role : EmployeeRole.STAFF);
        employee.setStatus(EmployeeStatus.ACTIVE);
        return employee;
    }

    /**
     * Tạo Quản trị viên hệ thống (Dùng cho seeding dữ liệu)
     */
    public Employee createAdmin(String username, String passwordHash, String email, String fullName) {
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setPasswordHash(passwordHash); // Giả định hash đã được xử lý hoặc dùng legacy
        employee.setEmail(email);
        employee.setFullName(fullName);
        employee.setRole(EmployeeRole.ADMIN);
        employee.setStatus(EmployeeStatus.ACTIVE);
        return employee;
    }
}
