package com.kozikwlodarski.backendgymflex.repository;

import com.kozikwlodarski.backendgymflex.entity.PaymentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentsEntity, Long> {
    // Pobranie płatności przypisanej do konkretnego karnetu (One-to-One)
    Optional<PaymentsEntity> findByMembershipEntityId(Long membershipId);
}