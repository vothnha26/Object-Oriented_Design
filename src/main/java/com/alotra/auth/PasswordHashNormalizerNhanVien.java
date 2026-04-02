package com.alotra.auth;

import com.alotra.entity.Employee;
import com.alotra.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHashNormalizerNhanVien {
    private final EmployeeRepository repository;
    private final PasswordEncoder passwordEncoder;

    public PasswordHashNormalizerNhanVien(EmployeeRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public void normalize(Integer id, String plainPassword) {
        Employee entity = repository.findById(id).orElse(null);
        if (entity != null) {
            String hash = entity.getPasswordHash();
            if (hash == null || !hash.startsWith("$2a$")) {
                entity.setPasswordHash(passwordEncoder.encode(plainPassword));
                repository.save(entity);
            }
        }
    }
}
