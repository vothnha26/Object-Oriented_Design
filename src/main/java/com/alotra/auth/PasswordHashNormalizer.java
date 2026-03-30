package com.alotra.auth;

import com.alotra.repository.CustomerRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHashNormalizer implements ApplicationRunner {
    private final CustomerRepository repo;
    private final PasswordEncoder encoder;

    public PasswordHashNormalizer(CustomerRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        repo.findAll().forEach(kh -> {
            String hash = kh.getPasswordHash();
            if (hash == null)
                return;
            // If not BCrypt (doesn't start with $2) then encode once
            if (!hash.startsWith("$2a$") && !hash.startsWith("$2b$") && !hash.startsWith("$2y$")) {
                kh.setPasswordHash(encoder.encode(hash));
                repo.save(kh);
            }
        });
    }
}
