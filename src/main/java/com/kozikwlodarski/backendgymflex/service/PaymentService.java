package com.kozikwlodarski.backendgymflex.service;

import com.kozikwlodarski.backendgymflex.entity.PaymentsEntity;
import com.kozikwlodarski.backendgymflex.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * Pobiera historię wszystkich płatności.
     */
    public List<PaymentsEntity> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * Pobiera szczegóły płatności na podstawie jej ID.
     */
    public PaymentsEntity getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono płatności o ID: " + id));
    }
}