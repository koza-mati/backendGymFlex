package com.kozikwlodarski.backendgymflex.service;

import com.kozikwlodarski.backendgymflex.repository.FitnessClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kozikwlodarski.backendgymflex.repository.ReservationRepository;
import com.kozikwlodarski.backendgymflex.dto.response.ClassOccupancyResponseDto;
import com.kozikwlodarski.backendgymflex.entity.FitnessClassEntity;
import java.time.LocalDateTime;
import java.util.List;
import com.kozikwlodarski.backendgymflex.repository.UserRepository;
import com.kozikwlodarski.backendgymflex.dto.request.FitnessClassRequestDto;
import com.kozikwlodarski.backendgymflex.dto.response.FitnessClassResponseDto;
import com.kozikwlodarski.backendgymflex.dto.response.ReservationResponseDto;
import com.kozikwlodarski.backendgymflex.dto.request.AttendanceRequestDto;
import com.kozikwlodarski.backendgymflex.entity.ReservationEntity;
import com.kozikwlodarski.backendgymflex.repository.AuditLogRepository;
import com.kozikwlodarski.backendgymflex.repository.ClassOccupancyProjection;
import com.kozikwlodarski.backendgymflex.entity.AuditLogEntity;
import com.kozikwlodarski.backendgymflex.event.ClassCancelledEvent;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Serwis zarządzający grafikiem zajęć fitness.
 */
@Service
@RequiredArgsConstructor
public class FitnessClassService {

    private final FitnessClassRepository fitnessClassRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<FitnessClassResponseDto> getSchedule() {
        return fitnessClassRepository.findAllByIsActiveTrueAndStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now())
                .stream().map(this::toScheduleResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FitnessClassResponseDto> getTrainerSchedule(String email) {
        return fitnessClassRepository.findAllByTrainerEmailAndIsActiveTrueOrderByStartTimeAsc(email)
                .stream().map(this::toScheduleResponse).toList();
    }

    @Transactional
    public FitnessClassResponseDto createClass(FitnessClassRequestDto request, String trainerEmail) {
        var actor = userRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono trenera."));
        if (!"ADMIN".equals(actor.getRole()) && !"TRAINER".equals(actor.getRole())) {
            throw new IllegalStateException("Zajęcia może utworzyć trener lub administrator.");
        }
        var trainer = actor;
        if ("ADMIN".equals(actor.getRole()) && request.getTrainerId() != null) {
            trainer = userRepository.findById(request.getTrainerId())
                    .filter(user -> "TRAINER".equals(user.getRole()) && user.isActive())
                    .orElseThrow(() -> new IllegalArgumentException("Wybrane konto nie jest aktywnym trenerem."));
        } else if ("TRAINER".equals(actor.getRole()) && request.getTrainerId() != null
                && !request.getTrainerId().equals(actor.getId())) {
            throw new IllegalStateException("Trener może przypisać zajęcia wyłącznie do siebie.");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("Koniec zajęć musi być późniejszy niż ich początek.");
        }
        FitnessClassEntity entity = new FitnessClassEntity();
        entity.setTrainer(trainer);
        entity.setTitle(request.getTitle());
        entity.setMaxCapacity(request.getMaxCapacity());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setTrainer(trainer);
        return toScheduleResponse(fitnessClassRepository.save(entity));
    }

    @Transactional
    public FitnessClassResponseDto updateClass(Long classId, FitnessClassRequestDto request, String adminEmail) {
        if (!"ADMIN".equals(userRepository.findByEmail(adminEmail).orElseThrow().getRole())) {
            throw new IllegalStateException("Grafik może edytować tylko administrator.");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("Koniec zajęć musi być późniejszy niż ich początek.");
        }
        FitnessClassEntity entity = fitnessClassRepository.findById(classId)
                .filter(FitnessClassEntity::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono aktywnych zajęć."));
        if (request.getMaxCapacity() < reservationRepository.countByFitnessClassIdAndStatus(classId, "CONFIRMED")) {
            throw new IllegalStateException("Limit miejsc nie może być mniejszy niż liczba aktualnych rezerwacji.");
        }
        entity.setTitle(request.getTitle());
        entity.setMaxCapacity(request.getMaxCapacity());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        if (request.getTrainerId() != null) {
            entity.setTrainer(userRepository.findById(request.getTrainerId())
                    .filter(user -> "TRAINER".equals(user.getRole()) && user.isActive())
                    .orElseThrow(() -> new IllegalArgumentException("Wybrane konto nie jest aktywnym trenerem.")));
        }
        return toScheduleResponse(entity);
    }

    @Transactional
    public void markAttendance(Long classId, Long reservationId, String trainerEmail, AttendanceRequestDto request) {
        FitnessClassEntity fitnessClass = fitnessClassRepository.findById(classId)
                .filter(FitnessClassEntity::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono aktywnych zajęć."));
        if (!"ADMIN".equals(userRepository.findByEmail(trainerEmail).orElseThrow().getRole())
                && !fitnessClass.getTrainer().getEmail().equals(trainerEmail)) {
            throw new IllegalStateException("Możesz oznaczać obecność tylko na własnych zajęciach.");
        }
        if (fitnessClass.getEndTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Obecność można oznaczyć po zakończeniu zajęć.");
        }
        ReservationEntity reservation = reservationRepository.findByIdAndFitnessClassId(reservationId, classId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono rezerwacji dla tych zajęć."));
        if (!"CONFIRMED".equals(reservation.getStatus())) {
            throw new IllegalStateException("Można oznaczyć tylko potwierdzoną rezerwację.");
        }
        reservation.setStatus(request.getStatus());
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setUsers(userRepository.findByEmail(trainerEmail).orElseThrow());
        auditLog.setAction("ATTENDANCE_MARKED");
        auditLog.setEntityName("RESERVATIONS");
        auditLog.setEntityId(reservationId);
        auditLog.setDetails("Oznaczono status: " + request.getStatus());
        auditLogRepository.save(auditLog);
    }

    private FitnessClassResponseDto toScheduleResponse(FitnessClassEntity entity) {
        FitnessClassResponseDto dto = new FitnessClassResponseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setTrainer(entity.getTrainer().getFirstName() + " " + entity.getTrainer().getLastName());
        dto.setTrainerId(entity.getTrainer().getId());
        dto.setMaxCapacity(entity.getMaxCapacity());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setActive(entity.isActive());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getClassParticipants(Long classId, String trainerEmail) {
        FitnessClassEntity fitnessClass = fitnessClassRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zajęć."));
        if (!"ADMIN".equals(userRepository.findByEmail(trainerEmail).orElseThrow().getRole())
                && !fitnessClass.getTrainer().getEmail().equals(trainerEmail)) {
            throw new IllegalStateException("Możesz przeglądać uczestników tylko własnych zajęć.");
        }
        return reservationRepository.findByFitnessClassIdAndStatus(classId, "CONFIRMED").stream().map(reservation -> {
            ReservationResponseDto dto = new ReservationResponseDto();
            dto.setReservationId(reservation.getId());
            dto.setUserId(reservation.getUsers().getId());
            dto.setUserFullName(reservation.getUsers().getFirstName() + " " + reservation.getUsers().getLastName());
            dto.setClassName(fitnessClass.getTitle());
            dto.setStartTime(fitnessClass.getStartTime());
            dto.setStatus(reservation.getStatus());
            dto.setTrainerFullName(fitnessClass.getTrainer().getFirstName() + " " + fitnessClass.getTrainer().getLastName());
            return dto;
        }).toList();
    }

    public List<ClassOccupancyResponseDto> getUpcomingOccupancy(String sort) {
        List<ClassOccupancyProjection> rows = "occupancy".equalsIgnoreCase(sort)
                ? fitnessClassRepository.findUpcomingOccupancyByPercentage()
                : fitnessClassRepository.findUpcomingOccupancyByStartTime();
        return rows.stream().map(row -> {
            ClassOccupancyResponseDto dto = new ClassOccupancyResponseDto();
            dto.setClassId(row.getClassId());
            dto.setTitle(row.getTitle());
            dto.setStartTime(row.getStartTime());
            dto.setTrainer(row.getTrainer());
            dto.setMaxCapacity(row.getMaxCapacity());
            dto.setBooked(row.getBooked().intValue());
            dto.setOccupancyPct(row.getOccupancyPct());
            return dto;
        }).toList();
    }

    /**
     * Odwoływanie zajęć przez administratora.
     * Metoda nie zawiera skomplikowanej logiki w Javie, ponieważ deleguje całe zadanie
     * do zdefiniowanej w bazie PostgreSQL procedury składowanej 'cancel_class'.
     *
     * Zgodnie z dokumentacją projektu, procedura ta wykonuje się atomowo w bazie danych i:
     * 1. Weryfikuje rolę p_admin_id (sprawdza czy to faktycznie ADMIN).
     * 2. Oznacza zajęcia jako is_active = false (miękkie usuwanie).
     * 3. Zmienia statusy potwierdzonych rezerwacji na 'CANCELLED'.
     * 4. Tworzy wpis w audit_logs ze zliczoną liczbą anulowanych rezerwacji.
     *
     * @param classId identyfikator odwoływanych zajęć
     * @param adminId identyfikator administratora wykonującego akcję
     */
    @Transactional
    public void cancelClass(Long classId, String adminEmail) {
        var admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono administratora."));
        if (!"ADMIN".equals(admin.getRole())) {
            throw new IllegalStateException("Tylko administrator może odwołać zajęcia.");
        }
        FitnessClassEntity fitnessClass = fitnessClassRepository.findById(classId)
                .filter(FitnessClassEntity::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Zajęcia nie istnieją lub są już anulowane."));
        List<String> recipients = reservationRepository.findByFitnessClassIdAndStatus(classId, "CONFIRMED")
                .stream().map(reservation -> reservation.getUsers().getEmail()).toList();
        fitnessClassRepository.cancelClass(classId, admin.getId());
        eventPublisher.publishEvent(new ClassCancelledEvent(fitnessClass.getTitle(), recipients));
    }
}
