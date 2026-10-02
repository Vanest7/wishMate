package com.gifts.demo.persistence.dao.db.jpa;

import com.gifts.demo.domain.model.ListWithCount;
import com.gifts.demo.domain.model.RefreshToken;
import com.gifts.demo.persistence.dao.db.RefreshTokenDaoDb;


import com.gifts.demo.persistence.dao.db.jpa.entity.RefreshTokenEntity;
import com.gifts.demo.persistence.dao.db.jpa.mapper.RefreshTokenJpaMapper;
import com.gifts.demo.persistence.dao.db.jpa.repository.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Primary
@RequiredArgsConstructor
public class RefreshTokenDaoJpa
        implements RefreshTokenDaoDb {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Override
    public List<RefreshToken> getAll() {
        return refreshTokenJpaRepository
                .findAll()
                .stream()
                .map(RefreshTokenJpaMapper.INSTANCE::toRefreshToken)
                .toList();
    }

    @Override
    public ListWithCount<RefreshToken> getAll(
            int page,
            int size
    ) {
        Page<RefreshTokenEntity> result =
                refreshTokenJpaRepository.findAll(
                        PageRequest.of(page, size)
                );

        List<RefreshToken> refreshTokens =
                result.getContent()
                        .stream()
                        .map(
                                RefreshTokenJpaMapper.INSTANCE
                                        ::toRefreshToken
                        )
                        .toList();

        return new ListWithCount<>(
                refreshTokens,
                result.getTotalElements()
        );
    }

    @Override
    public Optional<RefreshToken> findById(
            Integer id
    ) {
        return refreshTokenJpaRepository
                .findById(id)
                .map(
                        RefreshTokenJpaMapper.INSTANCE
                                ::toRefreshToken
                );
    }

    @Override
    public Integer insert(
            RefreshToken refreshToken
    ) {
        RefreshTokenEntity entity =
                RefreshTokenJpaMapper.INSTANCE
                        .toRefreshTokenEntity(
                                refreshToken
                        );

        RefreshTokenEntity savedEntity =
                refreshTokenJpaRepository.save(entity);

        return savedEntity.getId();
    }

    @Override
    public void update(
            RefreshToken refreshToken
    ) {
        RefreshTokenEntity entity =
                RefreshTokenJpaMapper.INSTANCE
                        .toRefreshTokenEntity(
                                refreshToken
                        );

        refreshTokenJpaRepository.save(entity);
    }

    @Override
    public void delete(Integer id) {
        refreshTokenJpaRepository.deleteById(id);
    }

    @Override
    public long count() {
        return refreshTokenJpaRepository.count();
    }

    @Override
    public RefreshToken save(
            RefreshToken refreshToken
    ) {
        RefreshTokenEntity entity =
                RefreshTokenJpaMapper.INSTANCE
                        .toRefreshTokenEntity(
                                refreshToken
                        );

        RefreshTokenEntity savedEntity =
                refreshTokenJpaRepository.save(entity);

        return RefreshTokenJpaMapper.INSTANCE
                .toRefreshToken(savedEntity);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(
            String tokenHash
    ) {
        return refreshTokenJpaRepository
                .findByTokenHash(tokenHash)
                .map(
                        RefreshTokenJpaMapper.INSTANCE
                                ::toRefreshToken
                );
    }

    @Override
    public List<RefreshToken> findByFamilyId(
            String familyId
    ) {
        return refreshTokenJpaRepository
                .findAllByFamilyId(familyId)
                .stream()
                .map(
                        RefreshTokenJpaMapper.INSTANCE
                                ::toRefreshToken
                )
                .toList();
    }

    @Override
    public List<RefreshToken> findByUserId(
            Integer userId
    ) {
        return refreshTokenJpaRepository
                .findAllByUser_Id(userId)
                .stream()
                .map(
                        RefreshTokenJpaMapper.INSTANCE
                                ::toRefreshToken
                )
                .toList();
    }
}