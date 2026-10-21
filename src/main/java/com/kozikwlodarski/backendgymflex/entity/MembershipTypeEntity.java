package com.kozikwlodarski.backendgymflex.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "membership_types")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MembershipTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private double price;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "membershipType")
    private List<MembershipEntity> membershipEntities;

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