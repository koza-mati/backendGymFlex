package com.kozikwlodarski.backendgymflex.repository;

import com.kozikwlodarski.backendgymflex.entity.FitnessClassEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FitnessClassRepository extends JpaRepository<FitnessClassEntity, Long> {

    // 1. Grafik zajęć w podanym przedziale czasowym
    List<FitnessClassEntity> findAllByStartTimeBetweenAndIsActiveTrue(LocalDateTime start, LocalDateTime end);

    // 2. BLOKADA PESYMISTYCZNA dla Transakcji 1 (WNF.06) - zapobiega overbookingowi
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FitnessClassEntity f WHERE f.id = :id AND f.isActive = true")
    Optional<FitnessClassEntity> findByIdAndLockForUpdate(@Param("id") Long id);

    // 3. Wywołanie procedury składowanej PostgreSQL (cancel_class)
    @Procedure(procedureName = "cancel_class")
    void cancelClass(@Param("p_class_id") Long classId, @Param("p_admin_id") Long adminId);
}