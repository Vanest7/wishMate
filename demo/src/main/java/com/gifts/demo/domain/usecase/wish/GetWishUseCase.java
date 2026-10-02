package com.gifts.demo.domain.usecase.wish;

import com.gifts.demo.domain.model.Wish;

public interface GetWishUseCase {

    Wish execute(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    );
}