package com.gifts.demo.persistence.dao.db;

import com.gifts.demo.domain.model.RefreshToken;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenDaoDb
        extends GenericDaoDb<RefreshToken> {

    Optional<RefreshToken> findByTokenHash(
            String tokenHash
    );

    List<RefreshToken> findByFamilyId(
            String familyId
    );

    List<RefreshToken> findByUserId(
            Integer userId
    );
}