package com.gifts.demo.domain.service;

import com.gifts.demo.domain.model.Wish;

import java.util.List;

public interface WishService {

    Wish getById(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    );

    List<Wish> getByEventId(
            Integer groupId,
            Integer eventId,
            Integer requesterId
    );

    List<Wish> getByUserIdAndEventId(
            Integer groupId,
            Integer eventId,
            Integer requesterId,
            Integer userId
    );

    Wish create(
            Integer groupId,
            Integer eventId,
            Integer requesterId,
            Wish wish
    );

    Wish update(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId,
            Wish wish
    );

    void delete(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    );
}