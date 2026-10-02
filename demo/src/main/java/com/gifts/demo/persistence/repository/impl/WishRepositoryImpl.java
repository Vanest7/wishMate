package com.gifts.demo.persistence.repository.impl;

import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.domain.repository.WishRepository;
import com.gifts.demo.persistence.dao.db.WishDaoDb;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WishRepositoryImpl implements WishRepository {

    private final WishDaoDb wishDaoDb;

    @Override
    public Optional<Wish> findById(Integer id) {
        return wishDaoDb.findById(id);
    }

    @Override
    public List<Wish> findByUserId(Integer userId) {
        return wishDaoDb.findByUserId(userId);
    }

    @Override
    public List<Wish> findByEventId(Integer eventId) {
        return wishDaoDb.findByEventId(eventId);
    }

    @Override
    public List<Wish> findByUserIdAndEventId(
            Integer userId,
            Integer eventId
    ) {
        return wishDaoDb.findByUserIdAndEventId(
                userId,
                eventId
        );
    }

    @Override
    public Optional<Wish> findByIdAndUserId(
            Integer wishId,
            Integer userId
    ) {
        return wishDaoDb.findByIdAndUserId(
                wishId,
                userId
        );
    }

    @Override
    public boolean existsByIdAndUserId(
            Integer wishId,
            Integer userId
    ) {
        return wishDaoDb.existsByIdAndUserId(
                wishId,
                userId
        );
    }

    @Override
    public Wish save(Wish wish) {
        return wishDaoDb.save(wish);
    }

    @Override
    public void delete(Integer id) {
        wishDaoDb.delete(id);
    }
}