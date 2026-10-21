package com.kozikwlodarski.backendgymflex.repository;

import com.kozikwlodarski.backendgymflex.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UsersEntity, Long>{
    // Do logowania przez email
    Optional<UsersEntity> findByEmail(String email);

    // Do sprawdzania unikalności przy rejestracji
    boolean existsByEmail(String email);

    // Do pobierania np. samych trenerów
    List<UsersEntity> findByRole(String role);

    // Zaawansowane zapytanie: Najaktywniejsi klienci (3+ treningów w 30 dni + opłacony karnet)
    @Query(value = "SELECT u.id, u.first_name, u.last_name, COUNT(r.id) AS trainings_30d " +
            "FROM users u " +
            "JOIN reservations r ON r.user_id = u.id " +
            "JOIN fitness_classes fc ON fc.id = r.class_id " +
            "WHERE r.status = 'ATTENDED' " +
            "  AND fc.start_time >= now() - INTERVAL '30 days' " +
            "  AND u.id IN ( " +
            "      SELECT m.user_id " +
            "      FROM membership m " +
            "      JOIN payments p ON p.membership_id = m.id " +
            "      WHERE p.status = 'PAID' " +
            "        AND m.is_active = true " +
            "        AND CURRENT_DATE BETWEEN m.start_date AND m.end_date " +
            "  ) " +
            "GROUP BY u.id, u.first_name, u.last_name " +
            "HAVING COUNT(r.id) >= 3 " +
            "ORDER BY trainings_30d DESC", nativeQuery = true)
    List<Object[]> findMostActiveClients();
}