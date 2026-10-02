package com.gifts.demo.persistence.dao.db.jpa.repository;

import com.gifts.demo.persistence.dao.db.jpa.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenJpaRepository
        extends JpaRepository<RefreshTokenEntity, Integer> {

    Optional<RefreshTokenEntity> findByTokenHash(
            String tokenHash
    );

    List<RefreshTokenEntity> findAllByFamilyId(
            String familyId
    );

    List<RefreshTokenEntity> findAllByUser_Id(
            Integer userId
    );
}