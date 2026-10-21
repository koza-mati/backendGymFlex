package com.kozikwlodarski.backendgymflex.repository;

import com.kozikwlodarski.backendgymflex.entity.MembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<MembershipEntity, Long> {

    // Historia karnetów danego użytkownika
    List<MembershipEntity> findByUsersId(Long usersId);

    // Szukanie najpóźniejszej daty wygaśnięcia karnetu dla danego usera
    // Wykorzystywane w Transakcji 2 (MembershipService) do przeliczania dat
    @Query("SELECT MAX(m.endDate) FROM MembershipEntity m WHERE m.users.id = :userId")
    Optional<LocalDate> findLatestEndDateForUser(@Param("userId") Long userId);

    // Sprawdzanie, czy klient ma aktywny karnet, który pokrywa podaną datę i ma status opłacony
    // Wykorzystywane w Transakcji 1 (ReservationService) do walidacji przed zapisem na zajęcia
    @Query("SELECT COUNT(m) > 0 FROM MembershipEntity m " +
            "WHERE m.users.id = :userId " +
            "AND m.isActive = true " +
            "AND :currentDate BETWEEN m.startDate AND m.endDate " +
            "AND EXISTS (SELECT p FROM PaymentsEntity p WHERE p.membershipEntity = m AND p.status = 'PAID')")
    boolean hasActiveAndPaidMembership(@Param("userId") Long userId, @Param("currentDate") LocalDate currentDate);
}