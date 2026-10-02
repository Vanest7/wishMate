package com.gifts.demo.persistence.dao.db.jpa.mapper;

import com.gifts.demo.domain.model.RefreshToken;
import com.gifts.demo.persistence.dao.db.jpa.entity.RefreshTokenEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = UserJpaMapper.class)
public interface RefreshTokenJpaMapper {

    RefreshTokenJpaMapper INSTANCE =
            Mappers.getMapper(RefreshTokenJpaMapper.class);

    RefreshToken toRefreshToken(
            RefreshTokenEntity refreshTokenEntity
    );

    RefreshTokenEntity toRefreshTokenEntity(
            RefreshToken refreshToken
    );
}