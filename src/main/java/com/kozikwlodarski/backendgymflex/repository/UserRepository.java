package com.kozikwlodarski.backendgymflex.repository;

import com.kozikwlodarski.backendgymflex.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UsersEntity, Long>{
    // Do logowania przez email
    Optional<UsersEntity> findByEmail(String email);

    // Do sprwadzania unikalności przy rejestracji
    boolean existsByEmail(String email);

    // Do pobierania np. samych trenerów
    List<UsersEntity> findByRole(String role);
}
