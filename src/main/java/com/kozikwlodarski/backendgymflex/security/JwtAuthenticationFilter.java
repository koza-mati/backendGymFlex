package com.kozikwlodarski.backendgymflex.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Filtr przechwytujący każde zapytanie HTTP i sprawdzający obecność tokenu JWT.
 * Chroni endpointy przed nieautoryzowanym dostępem.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return "/api/auth/login".equals(path)
                || "/api/auth/register".equals(path)
                || "/api/users/register".equals(path);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Pobranie nagłówka Authorization z zapytania
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Jeśli brakuje nagłówka lub nie zaczyna się od "Bearer ", puszczamy zapytanie dalej (np. do logowania)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Wycięcie samego tokenu (bez słowa "Bearer ")
        jwt = authHeader.substring(7);
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (JwtException | IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Nieprawidłowy token JWT.");
            return;
        }

        // 4. Jeśli w tokenie jest email i użytkownik nie jest jeszcze zalogowany w obecnym kontekście Springa
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Pobieramy dane użytkownika z bazy
            UserDetails userDetails;
            try {
                userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            } catch (UsernameNotFoundException exception) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Użytkownik tokenu już nie istnieje.");
                return;
            }

            // 5. Weryfikacja ważności tokenu (Wymóg WNF.04)
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Utworzenie obiektu autoryzacji
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Ręczne zalogowanie użytkownika na czas trwania tego konkretnego zapytania HTTP
                SecurityContextHolder.getContext().setAuthentication(authToken);
                response.setHeader("X-Auth-Token", jwtService.generateToken(userDetails));
            }
        }

        // 6. Przekazanie zapytania do kolejnych filtrów i docelowego kontrolera
        filterChain.doFilter(request, response);
    }
}
