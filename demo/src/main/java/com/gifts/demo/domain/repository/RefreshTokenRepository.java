package com.gifts.demo.domain.repository;

import com.gifts.demo.domain.model.RefreshToken;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository {

    Optional<RefreshToken> findById(
            Integer id
    );

    Optional<RefreshToken> findByTokenHash(
            String tokenHash
    );

    List<RefreshToken> findByFamilyId(
            String familyId
    );

    List<RefreshToken> findByUserId(
            Integer userId
    );

    RefreshToken save(
            RefreshToken refreshToken
    );

    void delete(
            Integer id
    );
}