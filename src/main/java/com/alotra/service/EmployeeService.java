package com.alotra.service;

import com.alotra.entity.Employee;
import com.alotra.entity.enums.EmployeeRole;
import com.alotra.entity.enums.EmployeeStatus;
import com.alotra.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository repo;
    private final PasswordEncoder encoder;

    public EmployeeService(EmployeeRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public List<Employee> findAll() { return repo.findAll(); }
    public Optional<Employee> findById(Integer id) { return repo.findById(id); }

    public List<Employee> findActive() { return repo.findByDeletedAtIsNull(); }

    public Employee saveHandlingPassword(Employee employee) {
        boolean isNew = employee.getId() == null;
        String pwd = employee.getPlainPassword();
        if (isNew) {
            if (pwd == null || pwd.isBlank()) {
                throw new IllegalArgumentException("Mật khẩu không được để trống cho nhân viên mới");
            }
            employee.setPasswordHash(encoder.encode(pwd));
        } else {
            if (pwd != null && !pwd.isBlank()) {
                employee.setPasswordHash(encoder.encode(pwd));
            } else {
                Employee current = repo.findById(employee.getId()).orElseThrow();
                employee.setPasswordHash(current.getPasswordHash());
            }
        }
        return repo.save(employee);
    }

    public void deleteById(Integer id) { repo.deleteById(id); }

    public Employee findByUsername(String u){ return repo.findByUsername(u).orElse(null);}    
    public Employee findByEmail(String e){ return repo.findByEmail(e).orElse(null);}    
    public Employee findByPhone(String p){ return repo.findByPhone(p).orElse(null);}   

    public List<Employee> search(String kw, EmployeeRole role, EmployeeStatus status) {
        if (kw != null && kw.isBlank()) kw = null;
        return repo.search(kw, role, status);
    }

    public String resetPassword(Integer id) {
        Employee employee = repo.findById(id).orElseThrow();
        String temp = generateTempPassword(10);
        employee.setPasswordHash(encoder.encode(temp));
        repo.save(employee);
        return temp;
    }

    private String generateTempPassword(int len) {
        final String dict = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(dict.charAt(r.nextInt(dict.length())));
        return sb.toString();
    }

    public void softDeleteToTrash(Integer id) {
        repo.findById(id).ifPresent(employee -> {
            employee.setDeletedAt(LocalDateTime.now());
            employee.setStatus(EmployeeStatus.INACTIVE);
            repo.save(employee);
        });
    }

    public void restoreFromTrash(Integer id) {
        repo.findById(id).ifPresent(employee -> {
            employee.setDeletedAt(null);
            repo.save(employee);
        });
    }
}
