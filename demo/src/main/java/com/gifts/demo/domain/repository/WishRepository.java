package com.gifts.demo.domain.repository;

import com.gifts.demo.domain.model.Wish;

import java.util.List;
import java.util.Optional;

public interface WishRepository {

    Optional<Wish> findById(Integer id);

    List<Wish> findByUserId(Integer userId);

    List<Wish> findByEventId(Integer eventId);

    List<Wish> findByUserIdAndEventId(
            Integer userId,
            Integer eventId
    );

    Optional<Wish> findByIdAndUserId(
            Integer wishId,
            Integer userId
    );

    boolean existsByIdAndUserId(
            Integer wishId,
            Integer userId
    );

    Wish save(Wish wish);

    void delete(Integer id);
}