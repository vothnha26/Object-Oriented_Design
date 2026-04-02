package com.alotra.factory;

import com.alotra.entity.Customer;
import com.alotra.entity.Employee;
import com.alotra.entity.User;
import com.alotra.entity.enums.CustomerStatus;
import com.alotra.entity.enums.EmployeeRole;
import com.alotra.entity.enums.EmployeeStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserFactory {

    public User createUser(String type, Map<String, Object> data) {
        if ("customer".equalsIgnoreCase(type)) {
            Customer customer = new Customer();
            customer.setUsername((String) data.get("username"));
            customer.setEmail((String) data.get("email"));
            String fullName = (String) data.get("fullName");
            customer.setFullName(fullName != null ? fullName.trim() : null);
            String phone = (String) data.get("phone");
            customer.setPhone(phone != null ? phone.trim() : null);
            CustomerStatus status = (CustomerStatus) data.get("status");
            customer.setStatus(status != null ? status : CustomerStatus.ACTIVE);
            customer.setPasswordHash((String) data.get("passwordHash"));
            return customer;
        } else if ("employee".equalsIgnoreCase(type)) {
            Employee employee = new Employee();
            employee.setUsername((String) data.get("username"));
            employee.setEmail((String) data.get("email"));
            String fullName = (String) data.get("fullName");
            employee.setFullName(fullName != null ? fullName.trim() : null);
            String phone = (String) data.get("phone");
            employee.setPhone(phone != null ? phone.trim() : null);
            EmployeeRole role = (EmployeeRole) data.get("role");
            employee.setRole(role != null ? role : EmployeeRole.STAFF);
            EmployeeStatus status = (EmployeeStatus) data.get("status");
            employee.setStatus(status != null ? status : EmployeeStatus.ACTIVE);
            employee.setPasswordHash((String) data.get("passwordHash"));
            return employee;
        }
        throw new IllegalArgumentException("Unknown user type: " + type);
    }

    public Employee createAdmin(String username, String passwordHash, String email, String fullName) {
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setPasswordHash(passwordHash);
        employee.setEmail(email);
        employee.setFullName(fullName);
        employee.setRole(EmployeeRole.ADMIN);
        employee.setStatus(EmployeeStatus.ACTIVE);
        return employee;
    }
}
