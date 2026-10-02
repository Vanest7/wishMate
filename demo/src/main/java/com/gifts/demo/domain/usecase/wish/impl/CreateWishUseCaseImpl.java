package com.gifts.demo.domain.usecase.wish.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.domain.service.WishService;
import com.gifts.demo.domain.usecase.wish.CreateWishUseCase;
import lombok.RequiredArgsConstructor;

@DomainUseCase
@RequiredArgsConstructor
public class CreateWishUseCaseImpl implements CreateWishUseCase {

    private final WishService wishService;

    @Override
    public Wish execute(
            Integer groupId,
            Integer eventId,
            Integer requesterId,
            Wish wish
    ) {
        return wishService.create(
                groupId,
                eventId,
                requesterId,
                wish
        );
    }
}