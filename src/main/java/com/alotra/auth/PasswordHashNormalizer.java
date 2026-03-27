package com.alotra.auth;

import com.alotra.entity.Customer;
import com.alotra.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PasswordHashNormalizer extends AbstractPasswordHashNormalizer<Customer> {
    private final CustomerRepository repo;

    public PasswordHashNormalizer(CustomerRepository repo, PasswordEncoder encoder) {
        super(encoder);
        this.repo = repo;
    }

    @Override
    protected List<Customer> findAll() {
        return repo.findAll();
    }

    @Override
    protected String getPasswordHash(Customer entity) {
        return entity.getPasswordHash();
    }

    @Override
    protected void setPasswordHash(Customer entity, String hash) {
        entity.setPasswordHash(hash);
    }

    @Override
    protected void save(Customer entity) {
        repo.save(entity);
    }
}
