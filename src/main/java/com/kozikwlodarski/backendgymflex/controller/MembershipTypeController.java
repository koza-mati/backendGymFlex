package com.kozikwlodarski.backendgymflex.controller;

import com.kozikwlodarski.backendgymflex.dto.request.MembershipTypeRequestDto;
import com.kozikwlodarski.backendgymflex.dto.response.MembershipTypeResponseDto;
import com.kozikwlodarski.backendgymflex.service.MembershipTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membership-types")
@RequiredArgsConstructor
public class MembershipTypeController {

    private final MembershipTypeService membershipTypeService;

    /**
     * Zwraca pełną ofertę klubu (tylko aktywne karnety do wyboru dla klienta).
     */
    @GetMapping
    public ResponseEntity<List<MembershipTypeResponseDto>> getAllActiveTypes() {
        return ResponseEntity.ok(membershipTypeService.getAllActiveTypes());
    }

    /**
     * Zwraca szczegóły konkretnego karnetu.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MembershipTypeResponseDto> getTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(membershipTypeService.getTypeById(id));
    }

    /**
     * Dodaje nowy typ karnetu do oferty klubu (wymaga uprawnień administratora).
     */
    @PostMapping
    public ResponseEntity<MembershipTypeResponseDto> createType(@Valid @RequestBody MembershipTypeRequestDto requestDto) {
        return new ResponseEntity<>(membershipTypeService.createType(requestDto), HttpStatus.CREATED);
    }

    /**
     * Aktualizuje parametry istniejącego karnetu w ofercie (zmiana ceny, długości).
     */
    @PutMapping("/{id}")
    public ResponseEntity<MembershipTypeResponseDto> updateType(@PathVariable Long id, @Valid @RequestBody MembershipTypeRequestDto requestDto) {
        return ResponseEntity.ok(membershipTypeService.updateType(id, requestDto));
    }

    /**
     * Wycofuje karnet ze sprzedaży (Soft Delete - is_active = false).
     * Nie narusza spójności bazy danych w już sprzedanych karnetach.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateType(@PathVariable Long id) {
        membershipTypeService.deActiveType(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}