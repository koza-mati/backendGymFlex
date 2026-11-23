package com.kozikwlodarski.backendgymflex.security;

import com.kozikwlodarski.backendgymflex.entity.UsersEntity;
import com.kozikwlodarski.backendgymflex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Serwis integrujący naszą bazę danych ze Spring Security.
 * Odpowiada za pobranie danych użytkownika na podstawie jego adresu e-mail.
 */
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Wymóg WF.03: Logowanie za pomocą adresu e-mail i hasła.
     * Metoda wyszukuje użytkownika w bazie i tłumaczy naszą encję (UsersEntity)
     * na obiekt zrozumiały dla Spring Security (UserDetails).
     *
     * @param email adres e-mail wpisany podczas logowania lub wyciągnięty z tokenu JWT
     * @return UserDetails obiekt reprezentujący uwierzytelnionego użytkownika z jego rolami
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsersEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + email));

        // Tworzymy obiekt użytkownika Spring Security.
        // Automatycznie przekazujemy jego status aktywności (is_active) oraz przypisujemy rolę (np. ROLE_CLIENT).
        return new User(
                user.getEmail(),
                user.getPasswordHash(),
                user.isActive(), // true = konto aktywne, false = zablokowane/usunięte
                true,
                true,
                true,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
