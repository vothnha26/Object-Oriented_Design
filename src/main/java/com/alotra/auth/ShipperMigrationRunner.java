package com.alotra.auth;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1) // Run before PasswordHashNormalizerNhanVien
public class ShipperMigrationRunner implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public ShipperMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Use raw SQL to avoid Hibernate's enum mapping crash
        String sql = "UPDATE Employee SET VaiTro = 'STAFF' WHERE VaiTro = 'SHIPPER'";
        int rows = jdbcTemplate.update(sql);
        
        if (rows > 0) {
            System.out.println(">>> SHIPPER MIGRATION: Successfully reassigned " + rows + " legacy shippers to STAFF role.");
        }
    }
}
