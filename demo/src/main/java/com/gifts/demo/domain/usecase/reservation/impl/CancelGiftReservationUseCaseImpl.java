package com.gifts.demo.domain.usecase.reservation.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.service.GiftReservationService;
import com.gifts.demo.domain.usecase.reservation.CancelGiftReservationUseCase;
import lombok.RequiredArgsConstructor;

@DomainUseCase
@RequiredArgsConstructor
public class CancelGiftReservationUseCaseImpl
        implements CancelGiftReservationUseCase {

    private final GiftReservationService giftReservationService;

    @Override
    public void execute(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    ) {
        giftReservationService.release(
                groupId,
                eventId,
                wishId,
                requesterId
        );
    }
}