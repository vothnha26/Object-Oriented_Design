package com.alotra.security;

import com.alotra.entity.Employee;
import com.alotra.entity.enums.EmployeeRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class EmployeeUserDetails implements UserDetails {
    private final Employee employee;

    public EmployeeUserDetails(Employee employee) { this.employee = employee; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (employee.getRole() == EmployeeRole.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if (employee.getRole() == EmployeeRole.SHIPPER) {
            return List.of(new SimpleGrantedAuthority("ROLE_SHIPPER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_VENDOR"));
    }

    @Override
    public String getPassword() { return employee.getPasswordHash(); }

    @Override
    public String getUsername() { return employee.getUsername(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return employee.isActive(); }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return employee.isActive(); }

    public Integer getId() { return employee.getId(); }
    public String getFullName() { return employee.getFullName(); }
    public String getEmail() { return employee.getEmail(); }
    public String getAvatarUrl() { return null; }

    public RoleView getRole() {
        if (employee.isAdmin()) return new RoleView("Quản trị viên");
        if (employee.isShipper()) return new RoleView("Nhân viên giao hàng");
        return new RoleView("Nhân viên");
    }

    public static class RoleView {
        private final String name;
        public RoleView(String name) { this.name = name; }
        public String getName() { return name; }
    }
}
