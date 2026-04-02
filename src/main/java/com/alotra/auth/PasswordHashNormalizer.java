package com.alotra.auth;

import com.alotra.entity.Customer;
import com.alotra.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHashNormalizer {
    private final CustomerRepository repository;
    private final PasswordEncoder passwordEncoder;

    public PasswordHashNormalizer(CustomerRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public void normalize(Integer id, String plainPassword) {
        Customer entity = repository.findById(id).orElse(null);
        if (entity != null) {
            String hash = entity.getPasswordHash();
            if (hash == null || !hash.startsWith("$2a$")) {
                entity.setPasswordHash(passwordEncoder.encode(plainPassword));
                repository.save(entity);
            }
        }
    }
}
