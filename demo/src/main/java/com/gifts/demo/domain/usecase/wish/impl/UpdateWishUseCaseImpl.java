package com.gifts.demo.domain.usecase.wish.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.domain.service.WishService;
import com.gifts.demo.domain.usecase.wish.UpdateWishUseCase;
import lombok.RequiredArgsConstructor;

@DomainUseCase
@RequiredArgsConstructor
public class UpdateWishUseCaseImpl implements UpdateWishUseCase {

    private final WishService wishService;

    @Override
    public Wish execute(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId,
            Wish wish
    ) {
        return wishService.update(
                groupId,
                eventId,
                wishId,
                requesterId,
                wish
        );
    }
}