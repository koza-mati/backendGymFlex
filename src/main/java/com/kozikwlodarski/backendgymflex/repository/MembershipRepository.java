package com.kozikwlodarski.backendgymflex.repository;


import com.kozikwlodarski.backendgymflex.entity.MembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipRepository extends JpaRepository<MembershipEntity, Long> {
    // Historia karnetów danego użytkownika
    List<MembershipEntity> findByUsersId(Long usersId);
}
