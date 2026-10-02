package com.gifts.demo.domain.usecase.reservation.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.GiftReservation;
import com.gifts.demo.domain.service.GiftReservationService;
import com.gifts.demo.domain.usecase.reservation.GetMyGiftReservationsUseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@DomainUseCase
@RequiredArgsConstructor
public class GetMyGiftReservationsUseCaseImpl
        implements GetMyGiftReservationsUseCase {

    private final GiftReservationService giftReservationService;

    @Override
    public List<GiftReservation> execute(Integer requesterId) {
        return giftReservationService.getMyReservations(requesterId);
    }
}