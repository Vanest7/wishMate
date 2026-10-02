package com.gifts.demo.domain.usecase.reservation;

import com.gifts.demo.domain.model.GiftReservation;

import java.util.List;

public interface GetMyGiftReservationsUseCase {

    List<GiftReservation> execute(Integer requesterId);
}