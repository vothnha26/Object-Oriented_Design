package com.alotra.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class LegacyPasswordEncoderAdapter implements PasswordEncoder {
    private static final String BCRYPT_PREFIX = "{bcrypt}";
    private static final String NOOP_PREFIX = "{noop}";

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        String stored = encodedPassword.trim();
        String raw = rawPassword == null ? "" : rawPassword.toString();
        if (stored.startsWith(BCRYPT_PREFIX)) {
            return bcrypt.matches(raw, stored.substring(BCRYPT_PREFIX.length()));
        }
        if (stored.startsWith(NOOP_PREFIX)) {
            return raw.equals(stored.substring(NOOP_PREFIX.length()));
        }
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return bcrypt.matches(raw, stored);
        }
        return raw.equals(stored);
    }
}
