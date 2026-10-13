package com.gifts.demo.domain.usecase.reservation;

import com.gifts.demo.domain.model.WishReservationStatus;

import java.util.Optional;

public interface GetWishReservationStatusUseCase {

    Optional<WishReservationStatus> execute(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    );
}
