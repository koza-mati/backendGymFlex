package com.kozikwlodarski.backendgymflex.config;

import com.kozikwlodarski.backendgymflex.entity.UsersEntity;
import com.kozikwlodarski.backendgymflex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LegacyPasswordHashMigration implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (UsersEntity user : userRepository.findAll()) {
            String storedPassword = user.getPasswordHash();
            if (!isBcryptHash(storedPassword)) {
                user.setPasswordHash(passwordEncoder.encode(storedPassword));
            }
        }
    }

    private boolean isBcryptHash(String value) {
        return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
