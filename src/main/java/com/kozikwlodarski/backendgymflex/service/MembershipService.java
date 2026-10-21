package com.kozikwlodarski.backendgymflex.service;

import com.kozikwlodarski.backendgymflex.dto.request.PurchaseMembershipRequestDto;
import com.kozikwlodarski.backendgymflex.dto.response.MembershipResponseDto;
import com.kozikwlodarski.backendgymflex.entity.*;
import com.kozikwlodarski.backendgymflex.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Serwis obsługujący logikę zarządzania posiadanymi karnetami klientów.
 */
@Service
@RequiredArgsConstructor
public class MembershipService {
    private final MembershipRepository membershipRepository;
    private final MembershipTypeRepository membershipTypeRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogRepository auditLogRepository;

    /**
     * TRANSAKCJA 2 (Wymóg WB.03): Zakup nowego karnetu.
     * Zapewnia spójność operacji (ACID). Zapisuje karnet, symuluje płatność i dodaje wpis do logów audytowych.
     * W przypadku jakiegokolwiek błędu, Spring wykonuje automatyczny ROLLBACK całej transakcji.
     *
     * @param requestDto dane wejściowe zakupu (ID użytkownika, ID typu karnetu, metoda płatności)
     * @return MembershipResponseDto podsumowanie utworzonego karnetu
     */
    @Transactional
    public MembershipResponseDto purchaseMembership(PurchaseMembershipRequestDto requestDto) {

        // 1. Walidacja użytkownika i cennika
        UsersEntity user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika o ID: " + requestDto.getUserId()));

        MembershipTypeEntity type = membershipTypeRepository.findById(requestDto.getMembershipTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono karnetu o ID: " + requestDto.getMembershipTypeId()));

        if (!type.isActive()) {
            throw new IllegalStateException("Wybrany typ karnetów nie jest obecnie dostępny w sprzedaży");
        }

        // 2. Przeliczanie dat ważności (Wymóg WB.03)
        // System automatycznie wylicza datę wygaśnięcia. Jeśli klient ma ważny karnet, nowy zaczyna się dzień po jego wygaśnięciu.
        LocalDate startDate = LocalDate.now();
        var latestEndDate = membershipRepository.findLatestEndDateForUser(user.getId());

        if (latestEndDate.isPresent() && latestEndDate.get().isAfter(startDate.minusDays(1))) {
            startDate = latestEndDate.get().plusDays(1);
        }

        LocalDate endDate = startDate.plusDays(type.getDurationDays() - 1);

        // 3. Utworzenie rekordu karnetu
        MembershipEntity membership = new MembershipEntity();
        membership.setUsers(user);
        membership.setMembershipType(type);
        membership.setStartDate(startDate);
        membership.setEndDate(endDate);
        membership.setActive(true);

        MembershipEntity savedMembership = membershipRepository.save(membership);

        // 4. Utworzenie płatności (zapis ceny historycznej chroni przed zmianą cennika w przyszłości)
        PaymentsEntity payment = new PaymentsEntity();
        payment.setMembershipEntity(savedMembership);
        payment.setAmount(BigDecimal.valueOf(type.getPrice()));
        payment.setMethod(requestDto.getPaymentMethod());
        payment.setStatus("PAID"); // Symulacja udanej płatności
        payment.setPaidAt(LocalDateTime.now());

        paymentRepository.save(payment);

        // 5. Ślad rewizyjny (Audit Log)
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setUsers(user);
        auditLog.setAction("MEMBERSHIP_PURCHASED");
        auditLog.setEntityName("MEMBERSHIP");
        auditLog.setEntityId(savedMembership.getId());
        auditLog.setDetails("Zakupiono karnet: " + type.getName());

        auditLogRepository.save(auditLog);

        // 6. Mapowanie odpowiedzi
        MembershipResponseDto response = new MembershipResponseDto();
        response.setMembershipId(savedMembership.getId());
        response.setMembershipTypeName(type.getName());
        response.setStartDate(savedMembership.getStartDate());
        response.setEndDate(savedMembership.getEndDate());
        response.setPricePaid(type.getPrice());
        response.setPaymentStatus("PAID");
        response.setActive(savedMembership.isActive());

        return response;
    }
}