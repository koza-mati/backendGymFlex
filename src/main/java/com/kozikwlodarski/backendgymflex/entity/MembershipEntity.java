package com.kozikwlodarski.backendgymflex.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembershipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UsersEntity users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_type_id", nullable = false)
    private MembershipTypeEntity membershipType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;


    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime upadtedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;


    //RELACJE
    @OneToOne(mappedBy = "payments")
    private List<Membership> memberships;

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
