package com.kozikwlodarski.backendgymflex.service;

import com.kozikwlodarski.backendgymflex.dto.request.CreateReservationRequestDto;
import com.kozikwlodarski.backendgymflex.dto.response.ReservationResponseDto;
import com.kozikwlodarski.backendgymflex.entity.*;
import com.kozikwlodarski.backendgymflex.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.security.Principal;

/**
 * Serwis do obsługi rezerwacji miejsc na zajęciach fitness.
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final FitnessClassRepository fitnessClassRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public java.util.List<ReservationResponseDto> getReservationsForUser(String email) {
        UsersEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zalogowanego użytkownika."));
        return reservationRepository.findByUsersId(user.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void cancelReservation(Long reservationId, String email) {
        ReservationEntity reservation = reservationRepository.findByIdAndUsersEmail(reservationId, email)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono rezerwacji."));
        if (!"CONFIRMED".equals(reservation.getStatus()) || !reservation.getFitnessClass().getStartTime().isAfter(java.time.LocalDateTime.now())) {
            throw new IllegalStateException("Rezerwację można anulować tylko przed rozpoczęciem zajęć.");
        }
        reservation.setStatus("CANCELLED");
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setUsers(reservation.getUsers());
        auditLog.setAction("RESERVATION_CANCELLED");
        auditLog.setEntityName("RESERVATIONS");
        auditLog.setEntityId(reservation.getId());
        auditLog.setDetails("Wypisanie z zajęć: " + reservation.getFitnessClass().getTitle());
        auditLogRepository.save(auditLog);
    }

    private ReservationResponseDto toResponse(ReservationEntity reservation) {
        ReservationResponseDto response = new ReservationResponseDto();
        response.setReservationId(reservation.getId());
        response.setUserId(reservation.getUsers().getId());
        response.setUserFullName(reservation.getUsers().getFirstName() + " " + reservation.getUsers().getLastName());
        response.setClassName(reservation.getFitnessClass().getTitle());
        response.setStartTime(reservation.getFitnessClass().getStartTime());
        response.setStatus(reservation.getStatus());
        if (reservation.getFitnessClass().getTrainer() != null) {
            response.setTrainerFullName(reservation.getFitnessClass().getTrainer().getFirstName() + " " + reservation.getFitnessClass().getTrainer().getLastName());
        }
        return response;
    }

    /**
     * TRANSAKCJA 1 (Wymóg WB.02): Zapis klienta na zajęcia.
     * System sprawdza dostępność miejsc i aktywność karnetu.
     * Wykorzystuje pesymistyczną blokadę (FOR UPDATE) chroniącą przed problemem overbookingu.
     *
     * @param requestDto dane rezerwacji (ID użytkownika i ID zajęć)
     * @return ReservationResponseDto potwierdzenie wygenerowanej rezerwacji
     */
    @Transactional
    public ReservationResponseDto createReservation(CreateReservationRequestDto requestDto, String email) {

        // 1. BLOKADA PESYMISTYCZNA (Wymóg WNF.06)
        // Zablokowanie rekordu zajęć w bazie. Inne transakcje próbujące zmodyfikować te zajęcia będą czekać na COMMIT bieżącej.
        FitnessClassEntity fitnessClass = fitnessClassRepository.findByIdAndLockForUpdate(requestDto.getFitnessClassId())
                .orElseThrow(() -> new IllegalArgumentException("Zajęcia nie istnieją lub są nieaktywne."));
        if (!fitnessClass.getStartTime().isAfter(java.time.LocalDateTime.now())) {
            throw new IllegalStateException("Nie można zapisać się na zajęcia, które już się rozpoczęły.");
        }

        UsersEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zalogowanego użytkownika."));

        // 2. Weryfikacja: czy klient ma aktywny, opłacony karnet ważny na dzień dzisiejszy?
        boolean hasActiveMembership = membershipRepository.hasActiveAndPaidMembership(user.getId(), LocalDate.now());
        if (!hasActiveMembership) {
            throw new IllegalStateException("Brak aktywnego i opłaconego karnetu.");
        }

        // 3. Weryfikacja: czy klient nie próbuje zapisać się podwójnie?
        boolean alreadyBooked = reservationRepository.existsByUsersIdAndFitnessClassIdAndStatus(user.getId(), fitnessClass.getId(), "CONFIRMED");
        if (alreadyBooked) {
            throw new IllegalStateException("Jesteś już zapisany na te zajęcia.");
        }

        // 4. Weryfikacja: sprawdzenie limitu miejsc (zabezpieczenie logiką aplikacyjną na zablokowanym wierszu)
        int currentBookings = reservationRepository.countByFitnessClassIdAndStatus(fitnessClass.getId(), "CONFIRMED");
        if (currentBookings >= fitnessClass.getMaxCapacity()) {
            throw new IllegalStateException("Brak wolnych miejsc na wybranych zajęciach.");
        }

        // 5. Utworzenie rezerwacji
        ReservationEntity reservation = new ReservationEntity();
        reservation.setUsers(user);
        reservation.setFitnessClass(fitnessClass);
        reservation.setStatus("CONFIRMED");

        ReservationEntity savedReservation = reservationRepository.save(reservation);

        // 6. Ślad rewizyjny (Audit Log)
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setUsers(user);
        auditLog.setAction("RESERVATION_CREATED");
        auditLog.setEntityName("RESERVATIONS");
        auditLog.setEntityId(savedReservation.getId());
        auditLog.setDetails("Zapis na zajęcia: " + fitnessClass.getTitle());

        auditLogRepository.save(auditLog);

        // 7. Budowanie bezpiecznego obiektu odpowiedzi
        ReservationResponseDto response = new ReservationResponseDto();
        response.setReservationId(savedReservation.getId());
        response.setUserId(user.getId());
        response.setUserFullName(user.getFirstName() + " " + user.getLastName());
        response.setClassName(fitnessClass.getTitle());
        response.setStartTime(fitnessClass.getStartTime());
        response.setStatus(savedReservation.getStatus());

        if (fitnessClass.getTrainer() != null) {
            response.setTrainerFullName(fitnessClass.getTrainer().getFirstName() + " " + fitnessClass.getTrainer().getLastName());
        }

        return response;
    }
}
