package com.kozikwlodarski.backendgymflex.repository;

import com.kozikwlodarski.backendgymflex.entity.FitnessClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FitnessClassRepository extends JpaRepository<FitnessClassEntity, Long> {
    // Grafik zajęć w podanym przedziale czasowym
    List<FitnessClassEntity> findByAllStartTimeBetweenAndIsActiveTrue(LocalDateTime start, LocalDateTime end);
}
