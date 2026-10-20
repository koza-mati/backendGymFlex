package com.kozikwlodarski.backendgymflex.repository;


import com.kozikwlodarski.backendgymflex.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    // Zliczenie potwierdzonych rezerwacji ( wykorzystywane w maxCapacity )
    int countByFitnessClassIdAndStatus(Long classId, String status);

    // Sprawdzanie czy użytkownik nie jest już zapisany
    boolean existsByUsersIdAndFitnessClassIdAndStatus(Long userId, Long classId, String status);

    // Rezerwacje konkretnego klineta
    List<ReservationEntity> findByUsersId(Long userId);
}
