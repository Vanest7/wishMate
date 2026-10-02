package com.gifts.demo.domain.service;

import com.gifts.demo.domain.model.GiftReservation;
import com.gifts.demo.domain.model.WishReservationStatus;

import java.util.Optional;

public interface GiftReservationService {

    GiftReservation reserve(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    );

    void release(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    );

    Optional<WishReservationStatus> getVisibleStatus(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    );
}