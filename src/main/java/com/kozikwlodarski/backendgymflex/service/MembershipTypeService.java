package com.kozikwlodarski.backendgymflex.service;

import com.kozikwlodarski.backendgymflex.dto.request.MembershipTypeRequestDto;
import com.kozikwlodarski.backendgymflex.dto.response.MembershipTypeResponseDto;
import com.kozikwlodarski.backendgymflex.entity.MembershipTypeEntity;
import com.kozikwlodarski.backendgymflex.repository.MembershipTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serwis zarządzający ofertą klubu (cennikiem).
 * Adnotacja @Transactional(readOnly = true) optymalizuje zapytania SELECT.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipTypeService {

    private final MembershipTypeRepository membershipTypeRepository;

    /**
     * Mapuje encję bazodanową na bezpieczny obiekt DTO zwracany na zewnątrz API.
     */
    private MembershipTypeResponseDto mapToResponseDto(MembershipTypeEntity entity) {
        MembershipTypeResponseDto dto = new MembershipTypeResponseDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPrice(entity.getPrice());
        dto.setDurationDays(entity.getDurationDays());
        dto.setActive(entity.isActive());
        return dto;
    }

    /**
     * Pobiera tylko te typy karnetów, które są aktualnie dostępne w sprzedaży.
     */
    public List<MembershipTypeResponseDto> getAllActiveTypes() {
        return membershipTypeRepository.findAllByIsActiveTrue().stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    public MembershipTypeResponseDto getTypeById(Long id) {
        MembershipTypeEntity entity = membershipTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("Typ karnetu o ID " + id + " nie istnieje"));
        return mapToResponseDto(entity);
    }

    /**
     * Tworzy nowy wpis w cenniku.
     */
    @Transactional
    public MembershipTypeResponseDto createType(MembershipTypeRequestDto requestDto) {
        MembershipTypeEntity entity = new MembershipTypeEntity();
        entity.setName(requestDto.getName());
        entity.setPrice(requestDto.getPrice());
        entity.setDurationDays(requestDto.getDurationDays());
        entity.setActive(true);

        MembershipTypeEntity savedEntity = membershipTypeRepository.save(entity);

        return mapToResponseDto(savedEntity);
    }

    /**
     * Aktualizuje parametry istniejącego karnetu.
     */
    @Transactional
    public MembershipTypeResponseDto updateType(Long id, MembershipTypeRequestDto requestDto) {
        MembershipTypeEntity entity = membershipTypeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Typ karnetu o ID " + id + " nie istnieje!"));
        entity.setName(requestDto.getName());
        entity.setPrice(requestDto.getPrice());
        entity.setDurationDays(requestDto.getDurationDays());

        // Zapis realizowany jest automatycznie przez mechanizm Dirty Checking w Hibernate
        return mapToResponseDto(entity);
    }

    /**
     * Soft Delete (Miękkie usuwanie).
     * Ukrywa karnet z oferty, ale zachowuje go w bazie dla spójności danych historycznych w tabelach Membership i Payments.
     */
    @Transactional
    public void deActiveType(Long id) {
        MembershipTypeEntity entity = membershipTypeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Typ karnetu o ID " + id + " nie istnieje"));
        entity.setActive(false);
    }
}