package com.alotra.service.account;

import com.alotra.entity.Customer;
import com.alotra.entity.enums.CustomerStatus;
import com.alotra.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }
    public Customer findByUsername(String username) {
        return customerRepository.findByUsername(username).orElse(null);
    }
    public Customer findByPhone(String phone) {
        return customerRepository.findByPhone(phone).orElse(null);
    }
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    public List<Customer> findAll() { return customerRepository.findAll(); }
    public Customer findById(Integer id) { return customerRepository.findById(id).orElse(null); }

    public List<Customer> search(String kw, CustomerStatus status) {
        if (kw != null && kw.isBlank()) kw = null;
        return customerRepository.search(kw, status);
    }

    public void deleteById(Integer id) { customerRepository.deleteById(id); }
}
