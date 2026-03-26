package com.alotra.security;

import com.alotra.entity.Employee;
import com.alotra.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EmployeeUserDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee nv = employeeRepository.findByUsername(username).orElse(null);
        if (nv == null) {
            nv = employeeRepository.findByEmail(username).orElse(null);
        }
        if (nv == null) {
            throw new UsernameNotFoundException("Không tìm thấy tài khoản nhân viên: " + username);
        }
        return new EmployeeUserDetails(nv);
    }
}
