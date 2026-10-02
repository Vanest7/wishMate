package com.gifts.demo.security.password;

import com.gifts.demo.domain.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringSecurityPasswordService implements PasswordService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        if (rawPassword == null || passwordHash == null
                || passwordHash.isBlank()) {
            return false;
        }

        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}