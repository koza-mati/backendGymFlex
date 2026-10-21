package com.kozikwlodarski.backendgymflex.controller;

import com.kozikwlodarski.backendgymflex.service.FitnessClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler REST obsługujący zarządzanie harmonogramem zajęć fitness w klubie.
 */
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class FitnessClassController {

    private final FitnessClassService fitnessClassService;

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
    public ResponseEntity<Void> cancelClass(@PathVariable Long classId, @RequestParam Long adminId) {
        fitnessClassService.cancelClass(classId, adminId);
        return ResponseEntity.ok().build();
    }
}