package com.kozikwlodarski.backendgymflex.controller;

import com.kozikwlodarski.backendgymflex.dto.response.PaymentResponseDto;
import com.kozikwlodarski.backendgymflex.dto.request.PaymentStatusRequestDto;
import jakarta.validation.Valid;
import com.kozikwlodarski.backendgymflex.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler REST do podglądu historii płatności.
 * Płatności są tworzone i aktualizowane automatycznie w Transakcji 2 (zakup karnetu).
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/mine")
    public ResponseEntity<List<PaymentResponseDto>> getMyPayments(java.security.Principal principal) {
        return ResponseEntity.ok(paymentService.getPaymentsForUser(principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponseDto>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentResponseDto> changePaymentStatus(@PathVariable Long id,
                                                                  @Valid @RequestBody PaymentStatusRequestDto request,
                                                                  java.security.Principal principal) {
        return ResponseEntity.ok(paymentService.changeStatus(id, request.getStatus(), principal.getName()));
    }
}
