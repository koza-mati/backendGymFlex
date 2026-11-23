package com.kozikwlodarski.backendgymflex.controller;

import com.kozikwlodarski.backendgymflex.dto.request.PurchaseMembershipRequestDto;
import com.kozikwlodarski.backendgymflex.dto.response.MembershipResponseDto;
import com.kozikwlodarski.backendgymflex.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

/**
 * Kontroler REST obsługujący procesy związane z przypisywaniem karnetów klientom.
 */
@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping("/mine")
    public ResponseEntity<List<MembershipResponseDto>> getMyMemberships(Principal principal) {
        return ResponseEntity.ok(membershipService.getMembershipsForUser(principal.getName()));
    }

    /**
     * TRANSAKCJA 2 (Wymóg WB.03): Zakup karnetu przez klienta.
     * Endpoint inicjuje operację, w której system wylicza datę wygaśnięcia, dodaje symulację płatności
     * i zapisuje ślad audytowy. Jeśli klient ma już karnet, daty nie będą się nakładać.
     *
     * @param requestDto zwalidowane dane o kupowanym karnecie i kliencie
     * @return 201 Created wraz z podsumowaniem zakupionego karnetu
     */
    @PostMapping("/purchase")
    public ResponseEntity<MembershipResponseDto> purchaseMembership(@Valid @RequestBody PurchaseMembershipRequestDto requestDto, Principal principal) {
        MembershipResponseDto response = membershipService.purchaseMembership(requestDto, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
