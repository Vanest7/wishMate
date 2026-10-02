package com.gifts.demo.persistence.dao.db;

import com.gifts.demo.domain.model.Wish;

import java.util.List;
import java.util.Optional;

public interface WishDaoDb extends GenericDaoDb<Wish> {

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

    void deleteByIdAndUserId(
            Integer wishId,
            Integer userId
    );
}