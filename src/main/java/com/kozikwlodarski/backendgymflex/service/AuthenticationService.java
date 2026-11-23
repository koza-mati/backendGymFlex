package com.kozikwlodarski.backendgymflex.service;

import com.kozikwlodarski.backendgymflex.dto.request.AuthenticationRequestDto;
import com.kozikwlodarski.backendgymflex.dto.response.AuthenticationResponseDto;
import com.kozikwlodarski.backendgymflex.entity.UsersEntity;
import com.kozikwlodarski.backendgymflex.repository.UserRepository;
import com.kozikwlodarski.backendgymflex.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // Jawny konstruktor (omijamy problemy z Lombokiem)
    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException exception) {
            UsersEntity legacyUser = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (legacyUser == null || isBcryptHash(legacyUser.getPasswordHash())
                    || !MessageDigest.isEqual(legacyUser.getPasswordHash().getBytes(StandardCharsets.UTF_8), request.getPassword().getBytes(StandardCharsets.UTF_8))) {
                throw exception;
            }
            legacyUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            userRepository.save(legacyUser);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        }

        UsersEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika"));

        var userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash()).authorities("ROLE_" + user.getRole()).build();
        var jwtToken = jwtService.generateToken(userDetails);

        return new AuthenticationResponseDto(jwtToken);
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
