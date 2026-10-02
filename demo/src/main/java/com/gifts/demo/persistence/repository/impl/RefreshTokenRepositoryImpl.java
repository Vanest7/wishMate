package com.gifts.demo.persistence.repository.impl;

import com.gifts.demo.domain.model.RefreshToken;
import com.gifts.demo.domain.repository.RefreshTokenRepository;

import com.gifts.demo.persistence.dao.db.RefreshTokenDaoDb;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl
        implements RefreshTokenRepository {

    private final RefreshTokenDaoDb refreshTokenDaoDb;

    @Override
    public Optional<RefreshToken> findById(
            Integer id
    ) {
        return refreshTokenDaoDb.findById(id);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(
            String tokenHash
    ) {
        return refreshTokenDaoDb.findByTokenHash(
                tokenHash
        );
    }

    @Override
    public List<RefreshToken> findByFamilyId(
            String familyId
    ) {
        return refreshTokenDaoDb.findByFamilyId(
                familyId
        );
    }

    @Override
    public List<RefreshToken> findByUserId(
            Integer userId
    ) {
        return refreshTokenDaoDb.findByUserId(
                userId
        );
    }

    @Override
    public RefreshToken save(
            RefreshToken refreshToken
    ) {
        return refreshTokenDaoDb.save(
                refreshToken
        );
    }

    @Override
    public void delete(Integer id) {
        refreshTokenDaoDb.delete(id);
    }
}