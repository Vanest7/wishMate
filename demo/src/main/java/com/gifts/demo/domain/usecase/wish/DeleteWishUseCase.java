package com.gifts.demo.domain.usecase.wish;

public interface DeleteWishUseCase {

    void execute(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    );
}