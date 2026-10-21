package com.kozikwlodarski.backendgymflex.service;

import com.kozikwlodarski.backendgymflex.dto.request.MembershipTypeRequestDto;
import com.kozikwlodarski.backendgymflex.dto.response.MembershipResponseDto;
import com.kozikwlodarski.backendgymflex.dto.response.MembershipTypeResponseDto;
import com.kozikwlodarski.backendgymflex.entity.MembershipEntity;
import com.kozikwlodarski.backendgymflex.entity.MembershipTypeEntity;
import com.kozikwlodarski.backendgymflex.repository.MembershipTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.Name;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipTypeService {

    private final MembershipTypeRepository membershipTypeRepository;

    // Metoda pomocnicza - mapowanie encji na DTO w celu lepszej kontroli
    private MembershipTypeResponseDto mapToResponseDto(MembershipTypeEntity entity){
        MembershipTypeResponseDto dto = new MembershipTypeResponseDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPrice(entity.getPrice());
        dto.setDurationDays(entity.getDuartionDays());
        dto.setActive(entity.isActive());
        return dto;
    }

    // 1. Pobieranie wszystkich aktywnych karentów
    public List<MembershipTypeResponseDto> getAllActiveTypes() {
        return membershipTypeRepository.findAllByIsActiveTrue().stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    // 2. Pobieranie konkretnego typu karnetu po ID
    public MembershipTypeResponseDto getTypeById(Long id){
        MembershipTypeEntity entity = membershipTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("Typ karnetu o ID " + id + " nie istnieje"));
    return mapToResponseDto(entity);
    }

    // 3. Dodanie nowego typu karnetu dla Admina
    @Transactional
    public MembershipTypeResponseDto createType(MembershipTypeRequestDto requestDto){
        // Mapowanie Request DTO -> Entity
        MembershipTypeEntity entity = new MembershipTypeEntity();
        entity.setName(requestDto.getName());
        entity.setPrice(entity.getPrice());
        entity.setDuartionDays(entity.getDuartionDays());
        entity.setActive(true);

        // Zapis w bazie danych
        MembershipTypeEntity savedEntity = membershipTypeRepository.save(entity);

        return mapToResponseDto(savedEntity);
    }

    // 4. Zaktualizowanie karnetu
    @Transactional
    public MembershipTypeResponseDto updateType(Long id, MembershipTypeRequestDto requestDto){
        MembershipTypeEntity entity = membershipTypeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Typ karnetu o ID " + id + " nie istnieje!"));
        entity.setName(requestDto.getName());
        entity.setPrice(requestDto.getPrice());
        entity.setDuartionDays(requestDto.getDurationDays());

        return mapToResponseDto(entity);
    }

    // 5. Soft Delete ukrywa karnet z oferty, ale pozostawai go w bazie dla zachowania spojnosci w histroaich platnosci
    @Transactional
    public void deActiveType(Long id){
        MembershipTypeEntity entity = membershipTypeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Typ karnetu o ID " + id + " nie istnieje"));
        entity.setActive(false);
    }
}
