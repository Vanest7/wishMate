package com.gifts.demo.domain.usecase.wish.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.domain.service.WishService;
import com.gifts.demo.domain.usecase.wish.GetWishesByEventUseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@DomainUseCase
@RequiredArgsConstructor
public class GetWishesByEventUseCaseImpl
        implements GetWishesByEventUseCase {

    private final WishService wishService;

    @Override
    public List<Wish> execute(
            Integer groupId,
            Integer eventId,
            Integer requesterId
    ) {
        return wishService.getByEventId(
                groupId,
                eventId,
                requesterId
        );
    }
}