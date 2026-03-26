package com.alotra.security;

import com.alotra.entity.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomerUserDetails implements UserDetails {
    private final Customer customer;

    public CustomerUserDetails(Customer customer) {
        this.customer = customer;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if ("boss".equalsIgnoreCase(customer.getUsername()) || "boss@alotra.com".equalsIgnoreCase(customer.getEmail())) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return customer.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return customer.getUsername();
    }

    public Integer getId() {
        return customer.getId();
    }

    public String getFullName() {
        return customer.getFullName();
    }

    public String getAvatarUrl() {
        return null;
    }

    public RoleView getRole() {
        boolean isAdmin = getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return new RoleView(isAdmin ? "Quản trị viên" : "Khách hàng");
    }

    public String getEmail() {
        return customer.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return customer.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return customer.isActive();
    }

    public Customer getCustomer() {
        return customer;
    }

    public static class RoleView {
        private final String name;
        public RoleView(String name) { this.name = name; }
        public String getName() { return name; }
    }
}
