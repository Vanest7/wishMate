package com.gifts.demo.domain.usecase.wish;

import com.gifts.demo.domain.model.Wish;

import java.util.List;

public interface GetWishesByUserUseCase {

    List<Wish> execute(
            Integer groupId,
            Integer eventId,
            Integer requesterId,
            Integer userId
    );
}