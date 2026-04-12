package com.alotra.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

public abstract class AbstractPasswordNormalizer<T> implements CommandLineRunner {
    protected final PasswordEncoder passwordEncoder;

    protected AbstractPasswordNormalizer(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<T> entities = findAll();
        for (T entity : entities) {
            String rawHash = getPasswordHash(entity);
            if (rawHash != null && !isBCrypt(rawHash)) {
                String newHash = passwordEncoder.encode(rawHash);
                setPasswordHash(entity, newHash);
                save(entity);
                System.out.println("[SEC] Đã chuẩn hóa mật khẩu cho entity: " + entity.getClass().getSimpleName());
            }
        }
    }

    private boolean isBCrypt(String hash) {
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }

    protected abstract List<T> findAll();
    protected abstract String getPasswordHash(T entity);
    protected abstract void setPasswordHash(T entity, String newHash);
    protected abstract void save(T entity);
}
