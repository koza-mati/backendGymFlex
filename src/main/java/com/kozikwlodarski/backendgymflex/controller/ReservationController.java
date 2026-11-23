package com.kozikwlodarski.backendgymflex.controller;

import com.kozikwlodarski.backendgymflex.dto.request.CreateReservationRequestDto;
import com.kozikwlodarski.backendgymflex.dto.response.ReservationResponseDto;
import com.kozikwlodarski.backendgymflex.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

/**
 * Kontroler REST obsługujący proces zapisywania się na zajęcia w klubie.
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * TRANSAKCJA 1 (Wymóg WB.02): Zapis na zajęcia.
     * Kluczowy endpoint aplikacji z punktu widzenia współbieżności. Wywołuje serwis używający
     * blokady wiersza (SELECT FOR UPDATE) w celu uniknięcia problemu overbookingu (Wymóg WNF.06).
     *
     * @param requestDto zwalidowane dane z ID klienta i ID zajęć
     * @return 201 Created wraz z obiektem potwierdzającym utworzenie rezerwacji
     */
    @PostMapping
    public ResponseEntity<ReservationResponseDto> createReservation(@Valid @RequestBody CreateReservationRequestDto requestDto, Principal principal) {
        ReservationResponseDto response = reservationService.createReservation(requestDto, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/mine")
    public ResponseEntity<java.util.List<ReservationResponseDto>> getMyReservations(Principal principal) {
        return ResponseEntity.ok(reservationService.getReservationsForUser(principal.getName()));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId, Principal principal) {
        reservationService.cancelReservation(reservationId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
