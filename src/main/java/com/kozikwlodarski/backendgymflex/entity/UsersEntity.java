package com.kozikwlodarski.backendgymflex.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table (name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsersEntity {
  @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "First_name", nullable = false, length = 50)
    private String FirstName;
    @Column(name = "Last_name", nullable = false, length = 50)
    private String LastName;
    @Column(name = "email", nullable = false, length = 110)
    private String email;
    @Column(name = "passwordHash", nullable = false, length = 255)
    private String passwordHash;
    @Column(name = "role", nullable = false, length = 50)
    private String role; // ADMIN, TRAINER, CLIENT

    // Cykl życia i pola audytowe
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

    // Relacje

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<MembershipEntity> memberships;

    @OneToMany(mappedBy = "trainer")
    private List<FitnessClassEntity> ledClass;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ReservationEntity> reservations;
}

