package com.gifts.demo.domain.usecase.reservation.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.GiftReservation;
import com.gifts.demo.domain.service.GiftReservationService;
import com.gifts.demo.domain.usecase.reservation.GetGiftReservationUseCase;
import lombok.RequiredArgsConstructor;

@DomainUseCase
@RequiredArgsConstructor
public class GetGiftReservationUseCaseImpl
        implements GetGiftReservationUseCase {

    private final GiftReservationService giftReservationService;

    @Override
    public GiftReservation execute(
            Integer reservationId,
            Integer requesterId
    ) {
        return giftReservationService.getById(
                reservationId,
                requesterId
        );
    }
}