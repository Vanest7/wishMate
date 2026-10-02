package com.gifts.demo.persistence.dao.db.jpa.mapper;

import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.persistence.dao.db.jpa.entity.WishEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {
        UserJpaMapper.class,
        EventJpaMapper.class
})
public interface WishJpaMapper {

    WishJpaMapper INSTANCE = Mappers.getMapper(WishJpaMapper.class);

    Wish toWish(WishEntity wishEntity);

    WishEntity toWishEntity(Wish wish);
}