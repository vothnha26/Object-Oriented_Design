package com.alotra.auth;

import com.alotra.entity.Employee;
import com.alotra.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PasswordHashNormalizerNhanVien extends AbstractPasswordHashNormalizer<Employee> {
    private final EmployeeRepository repo;

    public PasswordHashNormalizerNhanVien(EmployeeRepository repo, PasswordEncoder encoder) {
        super(encoder);
        this.repo = repo;
    }

    @Override
    protected List<Employee> findAll() {
        return repo.findAll();
    }

    @Override
    protected String getPasswordHash(Employee entity) {
        return entity.getPasswordHash();
    }

    @Override
    protected void setPasswordHash(Employee entity, String hash) {
        entity.setPasswordHash(hash);
    }

    @Override
    protected void save(Employee entity) {
        repo.save(entity);
    }
}
