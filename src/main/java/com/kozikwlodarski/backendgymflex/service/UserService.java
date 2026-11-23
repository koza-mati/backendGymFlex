package com.kozikwlodarski.backendgymflex.service;

import com.kozikwlodarski.backendgymflex.dto.request.UserRegisterRequestDto;
import com.kozikwlodarski.backendgymflex.dto.request.AdminUserCreateRequestDto;
import com.kozikwlodarski.backendgymflex.entity.UsersEntity;
import com.kozikwlodarski.backendgymflex.entity.AuditLogEntity;
import com.kozikwlodarski.backendgymflex.repository.AuditLogRepository;
import com.kozikwlodarski.backendgymflex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serwis zarządzający użytkownikami systemu (klientami, trenerami, administratorami).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogRepository auditLogRepository;

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
        user.setPasswordHash(passwordEncoder.encode(requestDto.getPassword()));

        user.setRole("CLIENT");
        user.setActive(true);

        return userRepository.save(user);
    }

    @Transactional
    public void createPrivilegedUser(AdminUserCreateRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Użytkownik o podanym adresie e-mail już istnieje.");
        }
        UsersEntity user = new UsersEntity();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Long userId, String adminEmail) {
        UsersEntity admin = userRepository.findByEmail(adminEmail)
                .filter(user -> "ADMIN".equals(user.getRole()) && user.isActive())
                .orElseThrow(() -> new IllegalStateException("Tylko aktywny administrator może wyłączać konta."));
        if (admin.getId().equals(userId)) {
            throw new IllegalStateException("Nie można wyłączyć własnego konta administratora.");
        }
        UsersEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika."));
        if (!user.isActive()) {
            return;
        }
        user.setActive(false);
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setUsers(admin);
        auditLog.setAction("USER_DEACTIVATED");
        auditLog.setEntityName("USERS");
        auditLog.setEntityId(user.getId());
        auditLog.setDetails("Wyłączono konto: " + user.getEmail());
        auditLogRepository.save(auditLog);
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

    public List<UsersEntity> getTrainers() {
        return userRepository.findByRole("TRAINER").stream().filter(UsersEntity::isActive).collect(Collectors.toList());
    }

    public List<UsersEntity> getAllUsers() {
        return userRepository.findAll();
    }
}
