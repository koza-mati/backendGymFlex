package com.kozikwlodarski.backendgymflex.repository;

import com.kozikwlodarski.backendgymflex.entity.MembershipEntity;
import com.kozikwlodarski.backendgymflex.entity.MembershipTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipTypeRepository extends JpaRepository<MembershipTypeEntity, Long> {
    List<MembershipEntity> findAllByIsActiveTrue();
}
