package com.gifts.demo.domain.usecase.reservation;

public interface CancelGiftReservationUseCase {

    void execute(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId);
}