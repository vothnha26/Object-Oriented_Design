package com.alotra.security;

import com.alotra.entity.Customer;
import com.alotra.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer kh = customerRepository.findByUsername(username).orElse(null);
        if (kh == null) {
            kh = customerRepository.findByEmail(username).orElse(null);
        }
        if (kh == null) {
            throw new UsernameNotFoundException("Không tìm thấy tài khoản: " + username);
        }
        return new CustomerUserDetails(kh);
    }
}
