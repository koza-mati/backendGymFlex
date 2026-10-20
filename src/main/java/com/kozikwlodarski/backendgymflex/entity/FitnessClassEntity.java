package com.kozikwlodarski.backendgymflex.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fitness_class")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FitnessClassEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private UsersEntity trainer;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name = "start_time", nullable = false)
    private LocalDate startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDate endTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @PrePersist
    private void createdOn() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void updatedOn() {
        this.updatedAt = LocalDateTime.now();
    }
}
