package com.kozikwlodarski.backendgymflex.repository;

import com.kozikwlodarski.backendgymflex.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    // Zliczenie potwierdzonych rezerwacji (wykorzystywane w maxCapacity)
    int countByFitnessClassIdAndStatus(Long classId, String status);

    // Sprawdzanie czy użytkownik nie jest już zapisany
    boolean existsByUsersIdAndFitnessClassIdAndStatus(Long userId, Long classId, String status);

    // Rezerwacje konkretnego klienta
    List<ReservationEntity> findByUsersId(Long userId);

    List<ReservationEntity> findByFitnessClassIdAndStatus(Long classId, String status);
    Optional<ReservationEntity> findByIdAndUsersEmail(Long id, String email);
    Optional<ReservationEntity> findByIdAndFitnessClassId(Long id, Long classId);
}
