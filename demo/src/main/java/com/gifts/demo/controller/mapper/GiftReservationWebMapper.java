package com.gifts.demo.controller.mapper;

import com.gifts.demo.controller.webmodel.reservation.GiftReservationResponse;
import com.gifts.demo.domain.model.GiftReservation;

import java.util.List;

public final class GiftReservationWebMapper {

    private GiftReservationWebMapper() {
    }

    public static GiftReservationResponse toResponse(
            GiftReservation reservation
    ) {
        return new GiftReservationResponse(
                reservation.getId(),
                reservation.getWish().getId(),
                reservation.getReservedAt()
        );
    }

    public static List<GiftReservationResponse> toResponseList(
            List<GiftReservation> reservations
    ) {
        return reservations.stream()
                .map(GiftReservationWebMapper::toResponse)
                .toList();
    }
}