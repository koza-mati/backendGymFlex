package com.kozikwlodarski.backendgymflex.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations", uniqueConstraints = @UniqueConstraint(
                name = "uq_user_class", columnNames = {"user_id", "class_id"}))
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UsersEntity users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private FitnessClassEntity fitnessClass;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime upadtedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @PrePersist
    private void createdOn() {
        this.createdAt = LocalDateTime.now();
        this.upadtedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void updatedOn() {
        this.upadtedAt = LocalDateTime.now();
    }
}


