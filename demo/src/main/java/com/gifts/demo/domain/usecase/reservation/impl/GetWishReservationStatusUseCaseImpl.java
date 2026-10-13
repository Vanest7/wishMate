package com.gifts.demo.domain.usecase.reservation.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.WishReservationStatus;
import com.gifts.demo.domain.service.GiftReservationService;
import com.gifts.demo.domain.usecase.reservation.GetWishReservationStatusUseCase;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@DomainUseCase
@RequiredArgsConstructor
public class GetWishReservationStatusUseCaseImpl
        implements GetWishReservationStatusUseCase {

    private final GiftReservationService giftReservationService;

    @Override
    public Optional<WishReservationStatus> execute(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    ) {
        return giftReservationService.getVisibleStatus(
                groupId,
                eventId,
                wishId,
                requesterId
        );
    }
}
