package com.kozikwlodarski.backendgymflex.controller;

import com.kozikwlodarski.backendgymflex.dto.request.UserRegisterRequestDto;
import com.kozikwlodarski.backendgymflex.entity.UsersEntity;
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
    public ResponseEntity<UsersEntity> registerUser(@Valid @RequestBody UserRegisterRequestDto requestDto) {
        UsersEntity createdUser = userService.registerUser(requestDto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
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