package com.gifts.demo.domain.repository;

import com.gifts.demo.domain.model.GiftReservation;

import java.util.List;
import java.util.Optional;

public interface GiftReservationRepository {

    Optional<GiftReservation> findById(Integer id);

    Optional<GiftReservation> findByWishId(Integer wishId);

    Optional<GiftReservation> findByWishIdAndUserId(
            Integer wishId,
            Integer userId
    );

    List<GiftReservation> findByUserId(Integer userId);

    boolean existsByWishId(Integer wishId);

    GiftReservation save(GiftReservation giftReservation);

    void delete(Integer id);
}