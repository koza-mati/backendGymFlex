package com.kozikwlodarski.backendgymflex.service;

import com.kozikwlodarski.backendgymflex.dto.request.CreateReservationRequestDto;
import com.kozikwlodarski.backendgymflex.dto.response.ReservationResponseDto;
import com.kozikwlodarski.backendgymflex.entity.*;
import com.kozikwlodarski.backendgymflex.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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

    /**
     * TRANSAKCJA 1 (Wymóg WB.02): Zapis klienta na zajęcia.
     * System sprawdza dostępność miejsc i aktywność karnetu.
     * Wykorzystuje pesymistyczną blokadę (FOR UPDATE) chroniącą przed problemem overbookingu.
     *
     * @param requestDto dane rezerwacji (ID użytkownika i ID zajęć)
     * @return ReservationResponseDto potwierdzenie wygenerowanej rezerwacji
     */
    @Transactional
    public ReservationResponseDto createReservation(CreateReservationRequestDto requestDto) {

        // 1. BLOKADA PESYMISTYCZNA (Wymóg WNF.06)
        // Zablokowanie rekordu zajęć w bazie. Inne transakcje próbujące zmodyfikować te zajęcia będą czekać na COMMIT bieżącej.
        FitnessClassEntity fitnessClass = fitnessClassRepository.findByIdAndLockForUpdate(requestDto.getFitnessClassId())
                .orElseThrow(() -> new IllegalArgumentException("Zajęcia nie istnieją lub są nieaktywne."));

        UsersEntity user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika o ID: " + requestDto.getUserId()));

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