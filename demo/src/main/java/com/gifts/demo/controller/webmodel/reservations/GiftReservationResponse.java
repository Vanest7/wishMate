package com.gifts.demo.controller.webmodel.reservation;

import java.time.LocalDateTime;

public record GiftReservationResponse(
        Integer id,
        Integer wishId,
        LocalDateTime reservedAt
) {
}