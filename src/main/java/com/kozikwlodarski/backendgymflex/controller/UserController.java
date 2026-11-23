package com.kozikwlodarski.backendgymflex.controller;

import com.kozikwlodarski.backendgymflex.dto.request.UserRegisterRequestDto;
import com.kozikwlodarski.backendgymflex.dto.request.AdminUserCreateRequestDto;
import com.kozikwlodarski.backendgymflex.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Wymóg WB.01: System musi umożliwiać rejestrację użytkowników.
     * Wymóg WF.01: Samodzielna rejestracja klienta przez formularz webowy.
     *
     * @param requestDto zwalidowane dane z formularza rejestracyjnego
     * @return 201 Created oraz obiekt zapisanego użytkownika
     */
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@Valid @RequestBody UserRegisterRequestDto requestDto) {
        userService.registerUser(requestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<Void> createPrivilegedUser(@Valid @RequestBody AdminUserCreateRequestDto request) {
        userService.createPrivilegedUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId, java.security.Principal principal) {
        userService.deactivateUser(userId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trainers")
    public ResponseEntity<List<com.kozikwlodarski.backendgymflex.dto.response.UserProfileResponseDto>> getTrainers() {
        return ResponseEntity.ok(userService.getTrainers().stream().map(user -> {
            var dto = new com.kozikwlodarski.backendgymflex.dto.response.UserProfileResponseDto();
            dto.setId(user.getId());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setEmail(user.getEmail());
            dto.setRole(user.getRole());
            return dto;
        }).toList());
    }

    @GetMapping("/admin")
    public ResponseEntity<List<com.kozikwlodarski.backendgymflex.dto.response.UserProfileResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream().map(user -> {
            var dto = new com.kozikwlodarski.backendgymflex.dto.response.UserProfileResponseDto();
            dto.setId(user.getId());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setEmail(user.getEmail());
            dto.setRole(user.getRole());
            dto.setActive(user.isActive());
            return dto;
        }).toList());
    }

    /**
     * Pobiera raport najaktywniejszych klientów klubu (3+ treningów w 30 dni + opłacony karnet).
     * Endpoint wykorzystuje zaawansowane natywne zapytanie SQL z podzapytaniem.
     *
     * @return 200 OK oraz lista klientów spełniających kryteria
     */
    @GetMapping("/report/most-active")
    public ResponseEntity<List<Object[]>> getMostActiveClientsReport() {
        return ResponseEntity.ok(userService.getMostActiveClientsReport());
    }
}
