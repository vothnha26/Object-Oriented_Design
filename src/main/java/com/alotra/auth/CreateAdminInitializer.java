package com.alotra.auth;

import com.alotra.entity.Employee;
import com.alotra.repository.EmployeeRepository;
import com.alotra.factory.UserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CreateAdminInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(CreateAdminInitializer.class);

    private final EmployeeRepository repo;
    private final UserFactory userFactory;

    public CreateAdminInitializer(EmployeeRepository repo, UserFactory userFactory) {
        this.repo = repo;
        this.userFactory = userFactory;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repo.findByUsername("boss").isPresent() || repo.findByEmail("boss@alotra.com").isPresent()) {
            log.info("Admin account 'boss' or email 'boss@alotra.com' already exists.");
            return;
        }
        
        Employee admin = userFactory.createAdmin("boss", "$2a$10$vly.EqIdXTPpjNbvghIsreZ.vXQuX6UpHz.X6X.vX6X.vX6X.vX6X", "boss@alotra.com", "System Admin");
        repo.save(admin);
        log.warn("Seeded default admin account: username='boss', password='123' (encoded).");
    }
}
