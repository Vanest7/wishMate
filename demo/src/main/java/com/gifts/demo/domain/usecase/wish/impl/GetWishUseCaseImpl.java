package com.gifts.demo.domain.usecase.wish.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.domain.service.WishService;
import com.gifts.demo.domain.usecase.wish.GetWishUseCase;
import lombok.RequiredArgsConstructor;

@DomainUseCase
@RequiredArgsConstructor
public class GetWishUseCaseImpl implements GetWishUseCase {

    private final WishService wishService;

    @Override
    public Wish execute(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    ) {
        return wishService.getById(
                groupId,
                eventId,
                wishId,
                requesterId
        );
    }
}