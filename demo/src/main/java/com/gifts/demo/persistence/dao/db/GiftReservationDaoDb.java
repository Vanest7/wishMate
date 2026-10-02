package com.gifts.demo.persistence.dao.db;

import com.gifts.demo.domain.model.GiftReservation;

import java.util.Optional;

public interface GiftReservationDaoDb
        extends GenericDaoDb<GiftReservation> {

    Optional<GiftReservation> findByWishId(Integer wishId);

    boolean existsByWishId(Integer wishId);

    Optional<GiftReservation> findByWishIdAndUserId(
            Integer wishId,
            Integer userId
    );
}