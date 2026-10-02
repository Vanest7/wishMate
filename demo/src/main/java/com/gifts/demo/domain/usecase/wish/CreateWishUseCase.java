package com.gifts.demo.domain.usecase.wish;

import com.gifts.demo.domain.model.Wish;

public interface CreateWishUseCase {

    Wish execute(
            Integer groupId,
            Integer eventId,
            Integer requesterId,
            Wish wish
    );
}