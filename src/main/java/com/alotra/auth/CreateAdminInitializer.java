package com.alotra.auth;

import com.alotra.entity.Customer;
import com.alotra.factory.UserFactory;
import com.alotra.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Ensure an admin account exists for initial access.
 * Username: boss, Password: 123
 */
@Component
public class CreateAdminInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(CreateAdminInitializer.class);

    private final CustomerRepository repo;
    private final UserFactory userFactory;

    public CreateAdminInitializer(CustomerRepository repo, UserFactory userFactory) {
        this.repo = repo;
        this.userFactory = userFactory;
    }

    @Override
    public void run(ApplicationArguments args) {
        // If a user with username 'boss' already exists, do nothing
        Customer existing = repo.findByUsername("boss").orElse(null);
        if (existing != null) {
            log.info("Admin account 'boss' already exists (id={}).", existing.getId());
            return;
        }
        // Also guard by email in case someone created it with email.
        if (repo.findByEmail("boss@alotra.com").isPresent()) {
            log.info("An account with email boss@alotra.com already exists; skipping admin seed.");
            return;
        }
        Customer admin = userFactory.createAdmin("boss", "boss@alotra.com", "123", "0900000000");
        repo.save(admin);
        log.warn("Seeded default admin account: username='boss', password='123'. Please change the password after first login.");
    }
}
