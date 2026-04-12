package com.alotra.util;

import com.alotra.entity.Customer;
import com.alotra.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CustomerPasswordNormalizer extends AbstractPasswordNormalizer<Customer> {
    private final CustomerRepository repository;

    public CustomerPasswordNormalizer(PasswordEncoder passwordEncoder, CustomerRepository repository) {
        super(passwordEncoder);
        this.repository = repository;
    }

    @Override
    protected List<Customer> findAll() {
        return repository.findAll();
    }

    @Override
    protected String getPasswordHash(Customer entity) {
        return entity.getPasswordHash();
    }

    @Override
    protected void setPasswordHash(Customer entity, String newHash) {
        entity.setPasswordHash(newHash);
    }

    @Override
    protected void save(Customer entity) {
        repository.save(entity);
    }
}
