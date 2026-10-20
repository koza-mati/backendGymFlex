package com.kozikwlodarski.backendgymflex.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UsersEntity users;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id", nullable = false, unique = true)
    private MembershipEntity membershipEntity;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "method", nullable = false, length = 30)
    private String method;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paid_At;

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
