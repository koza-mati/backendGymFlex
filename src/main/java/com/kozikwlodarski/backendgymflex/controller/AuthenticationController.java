package com.kozikwlodarski.backendgymflex.controller;

import com.kozikwlodarski.backendgymflex.dto.request.AuthenticationRequestDto;
import com.kozikwlodarski.backendgymflex.dto.response.AuthenticationResponseDto;
import com.kozikwlodarski.backendgymflex.service.AuthenticationService;
import com.kozikwlodarski.backendgymflex.service.UserService;
import com.kozikwlodarski.backendgymflex.dto.request.UserRegisterRequestDto;
import com.kozikwlodarski.backendgymflex.dto.response.UserProfileResponseDto;
import com.kozikwlodarski.backendgymflex.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> currentUser(java.security.Principal principal) {
        var user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika."));
        UserProfileResponseDto dto = new UserProfileResponseDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDto> register(@Valid @RequestBody UserRegisterRequestDto request) {
        userService.registerUser(request);
        AuthenticationRequestDto credentials = new AuthenticationRequestDto();
        credentials.setEmail(request.getEmail());
        credentials.setPassword(request.getPassword());
        return ResponseEntity.status(201).body(authenticationService.authenticate(credentials));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> login(@Valid @RequestBody AuthenticationRequestDto request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
