package com.kozikwlodarski.backendgymflex.service;

import com.kozikwlodarski.backendgymflex.dto.request.UserRegisterRequestDto;
import com.kozikwlodarski.backendgymflex.entity.UsersEntity;
import com.kozikwlodarski.backendgymflex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serwis zarządzający użytkownikami systemu (klientami, trenerami, administratorami).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * Wymóg WB.01: Rejestracja nowego użytkownika w systemie.
     * Metoda weryfikuje unikalność adresu e-mail (Wymóg WNF.03) i tworzy konto z domyślną rolą 'CLIENT'.
     * Hasło na tym etapie oczekuje na wdrożenie Spring Security (BCrypt - Wymóg WNF.01).
     *
     * @param requestDto dane z formularza rejestracyjnego
     * @return UsersEntity utworzona encja użytkownika
     */
    @Transactional
    public UsersEntity registerUser(UserRegisterRequestDto requestDto) {
        // Zabezpieczenie przed błędem bazy danych (Unique Constraint Violation)
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("Użytkownik o podanym adresie e-mail już istnieje.");
        }

        UsersEntity user = new UsersEntity();
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setEmail(requestDto.getEmail());

        // TODO: Wymóg WNF.01 - Integracja BCrypt przy wdrażaniu Spring Security
        user.setPasswordHash(requestDto.getPassword());

        user.setRole("CLIENT");
        user.setActive(true);

        return userRepository.save(user);
    }

    /**
     * Pobiera raport najaktywniejszych klientów.
     * Wykorzystuje natywne, zaawansowane zapytanie SQL agregujące dane (powyżej 3 treningów w 30 dni)
     * z podzapytaniem weryfikującym opłacony karnet, co jest wymagane w projekcie bazy danych.
     *
     * @return Lista tablic obiektów reprezentująca wiersze z bazy danych (id, imię, nazwisko, liczba treningów)
     */
    public List<Object[]> getMostActiveClientsReport() {
        return userRepository.findMostActiveClients();
    }
}