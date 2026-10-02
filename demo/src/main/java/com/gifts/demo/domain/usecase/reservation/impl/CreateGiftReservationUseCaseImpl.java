package com.gifts.demo.domain.usecase.reservation.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.GiftReservation;
import com.gifts.demo.domain.service.GiftReservationService;
import com.gifts.demo.domain.usecase.reservation.CreateGiftReservationUseCase;
import lombok.RequiredArgsConstructor;

@DomainUseCase
@RequiredArgsConstructor
public class CreateGiftReservationUseCaseImpl
        implements CreateGiftReservationUseCase {

    private final GiftReservationService giftReservationService;

    @Override
    public GiftReservation execute(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    ) {
        return giftReservationService.reserve(
                groupId,
                eventId,
                wishId,
                requesterId
        );
    }
}