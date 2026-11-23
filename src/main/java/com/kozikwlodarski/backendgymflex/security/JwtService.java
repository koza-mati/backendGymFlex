package com.kozikwlodarski.backendgymflex.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Serwis odpowiedzialny za operacje na tokenach JWT (generowanie, walidacja, odczyt).
 * Realizuje wymóg WNF.04 dotyczący sesji opartej na tokenach.
 */
@Service
@RequiredArgsConstructor
// <-- To słowo kluczowe likwiduje błąd "No beans of 'JwtService' type found"
public class JwtService {

    // Pobranie tajnego klucza z pliku application.properties
    @Value("${jwt.secret}")
    private String secretKey;

    // Pobranie czasu wygaśnięcia (60 minut) z pliku application.properties (Wymóg WNF.04)
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Wyciąga nazwę użytkownika (w naszym przypadku email) z tokenu.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generuje nowy token JWT dla uwierzytelnionego użytkownika.
     * Ustawia czas wydania oraz czas wygaśnięcia na 60 minut od teraz.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Wygasa po 60 minutach
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Weryfikuje, czy token należy do podanego użytkownika i czy nie wygasł.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && userDetails.isEnabled() && !isTokenExpired(token);
    }

    // --- Metody pomocnicze do dekodowania tokenu ---

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Zwraca klucz szyfrujący zdekodowany z BASE64.
     * Algorytm HMAC-SHA256 wymaga klucza o długości co najmniej 256 bitów.
     */
    private Key getSignInKey() {
        byte[] keyBytes = secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
