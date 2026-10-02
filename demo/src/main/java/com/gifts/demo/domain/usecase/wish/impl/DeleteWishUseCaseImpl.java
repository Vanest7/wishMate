package com.gifts.demo.domain.usecase.wish.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.service.WishService;
import com.gifts.demo.domain.usecase.wish.DeleteWishUseCase;
import lombok.RequiredArgsConstructor;

@DomainUseCase
@RequiredArgsConstructor
public class DeleteWishUseCaseImpl implements DeleteWishUseCase {

    private final WishService wishService;

    @Override
    public void execute(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    ) {
        wishService.delete(
                groupId,
                eventId,
                wishId,
                requesterId
        );
    }
}