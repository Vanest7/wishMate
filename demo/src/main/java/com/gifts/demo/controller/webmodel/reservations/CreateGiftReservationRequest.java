package com.gifts.demo.controller.webmodel.reservation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateGiftReservationRequest(
        @NotNull
        @Positive
        Integer wishId
) {
}