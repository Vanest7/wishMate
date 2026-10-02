package com.gifts.demo.domain.usecase.wish;

import com.gifts.demo.domain.model.Wish;

public interface UpdateWishUseCase {

    Wish execute(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId,
            Wish wish
    );
}