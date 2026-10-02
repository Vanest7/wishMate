package com.gifts.demo.domain.usecase.reservation;

import com.gifts.demo.domain.model.GiftReservation;

public interface CreateGiftReservationUseCase {

    GiftReservation execute(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    );
}