package com.gifts.demo.domain.usecase.wish;

import com.gifts.demo.domain.model.Wish;

import java.util.List;

public interface GetWishesByEventUseCase {

    List<Wish> execute(
            Integer groupId,
            Integer eventId,
            Integer requesterId
    );
}