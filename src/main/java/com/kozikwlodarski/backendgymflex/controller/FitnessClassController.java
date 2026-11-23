package com.kozikwlodarski.backendgymflex.controller;

import com.kozikwlodarski.backendgymflex.service.FitnessClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.kozikwlodarski.backendgymflex.dto.response.ClassOccupancyResponseDto;
import java.util.List;
import jakarta.validation.Valid;
import com.kozikwlodarski.backendgymflex.dto.request.FitnessClassRequestDto;
import com.kozikwlodarski.backendgymflex.dto.request.AttendanceRequestDto;
import com.kozikwlodarski.backendgymflex.dto.response.FitnessClassResponseDto;
import com.kozikwlodarski.backendgymflex.dto.response.ReservationResponseDto;

/**
 * Kontroler REST obsługujący zarządzanie harmonogramem zajęć fitness w klubie.
 */
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class FitnessClassController {

    private final FitnessClassService fitnessClassService;

    @GetMapping
    public ResponseEntity<List<FitnessClassResponseDto>> getSchedule() {
        return ResponseEntity.ok(fitnessClassService.getSchedule());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<FitnessClassResponseDto>> getTrainerSchedule(java.security.Principal principal) {
        return ResponseEntity.ok(fitnessClassService.getTrainerSchedule(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<FitnessClassResponseDto> createClass(@Valid @RequestBody FitnessClassRequestDto request,
                                                               java.security.Principal principal) {
        return ResponseEntity.status(201).body(fitnessClassService.createClass(request, principal.getName()));
    }

    @PutMapping("/{classId}")
    public ResponseEntity<FitnessClassResponseDto> updateClass(@PathVariable Long classId,
                                                               @Valid @RequestBody FitnessClassRequestDto request,
                                                               java.security.Principal principal) {
        return ResponseEntity.ok(fitnessClassService.updateClass(classId, request, principal.getName()));
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long classId, java.security.Principal principal) {
        fitnessClassService.cancelClass(classId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{classId}/participants")
    public ResponseEntity<List<ReservationResponseDto>> getParticipants(@PathVariable Long classId,
                                                                         java.security.Principal principal) {
        return ResponseEntity.ok(fitnessClassService.getClassParticipants(classId, principal.getName()));
    }

    @PutMapping("/{classId}/reservations/{reservationId}/attendance")
    public ResponseEntity<Void> markAttendance(@PathVariable Long classId, @PathVariable Long reservationId,
                                               @Valid @RequestBody AttendanceRequestDto request,
                                               java.security.Principal principal) {
        fitnessClassService.markAttendance(classId, reservationId, principal.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/occupancy")
    public ResponseEntity<List<ClassOccupancyResponseDto>> getOccupancy(@RequestParam(defaultValue = "startTime") String sort) {
        return ResponseEntity.ok(fitnessClassService.getUpcomingOccupancy(sort));
    }

    /**
     * Odwoływanie zajęć grupowych.
     * Wywołuje zdefiniowaną w systemie bazodanowym procedurę PL/pgSQL 'cancel_class'.
     * Zgodnie ze specyfikacją, wycofuje wszystkie rezerwacje z odwołanych zajęć i wykonuje wpis audytowy.
     *
     * @param classId identyfikator odwoływanych zajęć przekazany w ścieżce (URL)
     * @param adminId identyfikator administratora przekazany jako parametr (Query Param)
     * @return 200 OK po poprawnym wykonaniu procedury
     */
    @PostMapping("/{classId}/cancel")
    public ResponseEntity<Void> cancelClass(@PathVariable Long classId, java.security.Principal principal) {
        fitnessClassService.cancelClass(classId, principal.getName());
        return ResponseEntity.ok().build();
    }
}
