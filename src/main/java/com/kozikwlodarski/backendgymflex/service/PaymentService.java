package com.kozikwlodarski.backendgymflex.service;

import com.kozikwlodarski.backendgymflex.entity.PaymentsEntity;
import com.kozikwlodarski.backendgymflex.repository.PaymentRepository;
import com.kozikwlodarski.backendgymflex.repository.UserRepository;
import com.kozikwlodarski.backendgymflex.dto.response.PaymentResponseDto;
import com.kozikwlodarski.backendgymflex.repository.AuditLogRepository;
import com.kozikwlodarski.backendgymflex.entity.AuditLogEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    /**
     * Pobiera historię wszystkich płatności.
     */
    public List<PaymentResponseDto> getAllPayments() {
        return paymentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<PaymentResponseDto> getPaymentsForUser(String email) {
        return paymentRepository.findByMembershipEntityUsersEmail(email).stream().map(this::toResponse).toList();
    }

    /**
     * Pobiera szczegóły płatności na podstawie jej ID.
     */
    public PaymentResponseDto getPaymentById(Long id) {
        return toResponse(paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono płatności o ID: " + id)));
    }

    @Transactional
    public PaymentResponseDto changeStatus(Long id, String status, String actorEmail) {
        PaymentsEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono płatności o ID: " + id));
        if ("REFUNDED".equals(status) && !"PAID".equals(payment.getStatus())) {
            throw new IllegalStateException("Zwrot dotyczy wyłącznie opłaconej płatności.");
        }
        payment.setStatus(status);
        payment.setPaidAt("PAID".equals(status) ? LocalDateTime.now() : null);
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setUsers(userRepository.findByEmail(actorEmail).orElseThrow());
        auditLog.setAction("PAYMENT_STATUS_CHANGED");
        auditLog.setEntityName("PAYMENTS");
        auditLog.setEntityId(payment.getId());
        auditLog.setDetails("Nowy status płatności: " + status);
        auditLogRepository.save(auditLog);
        return toResponse(payment);
    }

    private PaymentResponseDto toResponse(PaymentsEntity payment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(payment.getId());
        dto.setMembershipId(payment.getMembershipEntity().getId());
        dto.setAmount(payment.getAmount());
        dto.setMethod(payment.getMethod());
        dto.setStatus(payment.getStatus());
        dto.setPaidAt(payment.getPaidAt());
        return dto;
    }
}
