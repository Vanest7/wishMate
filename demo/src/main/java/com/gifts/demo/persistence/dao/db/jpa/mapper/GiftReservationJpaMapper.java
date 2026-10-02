package com.gifts.demo.persistence.dao.db.jpa.mapper;

import com.gifts.demo.domain.model.GiftReservation;
import com.gifts.demo.persistence.dao.db.jpa.entity.GiftReservationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {
        UserJpaMapper.class,
        WishJpaMapper.class
})
public interface GiftReservationJpaMapper {

    GiftReservationJpaMapper INSTANCE =
            Mappers.getMapper(GiftReservationJpaMapper.class);

    GiftReservation toGiftReservation(
            GiftReservationEntity giftReservationEntity
    );

    GiftReservationEntity toGiftReservationEntity(
            GiftReservation giftReservation
    );
}
