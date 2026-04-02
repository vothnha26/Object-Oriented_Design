package com.alotra.auth;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

public abstract class AbstractPasswordHashNormalizer<T> implements ApplicationRunner {
    private final PasswordEncoder encoder;

    protected AbstractPasswordHashNormalizer(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public final void run(ApplicationArguments args) {
        findAll().forEach(entity -> {
            String hash = getPasswordHash(entity);
            if (hash == null) {
                return;
            }
            if (!isBcrypt(hash)) {
                setPasswordHash(entity, encoder.encode(hash));
                save(entity);
            }
        });
    }

    protected abstract List<T> findAll();

    protected abstract String getPasswordHash(T entity);

    protected abstract void setPasswordHash(T entity, String hash);

    protected abstract void save(T entity);

    private boolean isBcrypt(String hash) {
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }
}
