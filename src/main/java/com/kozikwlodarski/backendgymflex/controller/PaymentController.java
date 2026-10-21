package com.kozikwlodarski.backendgymflex.controller;

import com.kozikwlodarski.backendgymflex.entity.PaymentsEntity;
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

    @GetMapping
    public ResponseEntity<List<PaymentsEntity>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentsEntity> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }
}