package com.kozikwlodarski.backendgymflex.config;

import com.kozikwlodarski.backendgymflex.entity.UsersEntity;
import com.kozikwlodarski.backendgymflex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${gymflex.bootstrap-admin.email:}")
    private String email;
    @Value("${gymflex.bootstrap-admin.password:}")
    private String password;
    @Value("${gymflex.bootstrap-admin.first-name:Gym}")
    private String firstName;
    @Value("${gymflex.bootstrap-admin.last-name:Administrator}")
    private String lastName;

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Do bootstrapowania administratora wymagane są email i hasło.");
        }
        var existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            UsersEntity existingAdmin = existingUser.get();
            if (!"ADMIN".equals(existingAdmin.getRole())) {
                throw new IllegalStateException("Konto bootstrap administratora istnieje z inną rolą.");
            }
            if (!passwordEncoder.matches(password, existingAdmin.getPasswordHash())) {
                existingAdmin.setPasswordHash(passwordEncoder.encode(password));
                userRepository.save(existingAdmin);
            }
            return;
        }

        UsersEntity admin = new UsersEntity();
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole("ADMIN");
        admin.setActive(true);
        userRepository.save(admin);
    }
}
