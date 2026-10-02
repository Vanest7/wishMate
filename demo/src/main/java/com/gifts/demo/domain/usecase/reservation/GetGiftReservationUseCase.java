package com.gifts.demo.domain.usecase.reservation;

import com.gifts.demo.domain.model.GiftReservation;

public interface GetGiftReservationUseCase {

    GiftReservation execute(Integer reservationId, Integer requesterId);
}