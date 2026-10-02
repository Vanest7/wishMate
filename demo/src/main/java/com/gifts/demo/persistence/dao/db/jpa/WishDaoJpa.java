package com.gifts.demo.persistence.dao.db.jpa;

import com.gifts.demo.domain.model.ListWithCount;
import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.persistence.dao.db.WishDaoDb;
import com.gifts.demo.persistence.dao.db.jpa.entity.WishEntity;
import com.gifts.demo.persistence.dao.db.jpa.mapper.WishJpaMapper;
import com.gifts.demo.persistence.dao.db.jpa.repository.WishJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Primary
@RequiredArgsConstructor
public class WishDaoJpa implements WishDaoDb {

    private final WishJpaRepository wishJpaRepository;

    @Override
    public List<Wish> getAll() {
        return wishJpaRepository
                .findAll()
                .stream()
                .map(WishJpaMapper.INSTANCE::toWish)
                .toList();
    }

    @Override
    public ListWithCount<Wish> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WishEntity> wishPage = wishJpaRepository.findAll(pageable);

        return new ListWithCount<>(
                wishPage.stream()
                        .map(WishJpaMapper.INSTANCE::toWish)
                        .toList(),
                wishPage.getTotalElements()
        );
    }

    @Override
    public Optional<Wish> findById(Integer id) {
        return wishJpaRepository
                .findById(id)
                .map(WishJpaMapper.INSTANCE::toWish);
    }

    @Override
    public Integer insert(Wish wish) {
        WishEntity wishEntity =
                WishJpaMapper.INSTANCE.toWishEntity(wish);

        return wishJpaRepository
                .save(wishEntity)
                .getId();
    }

    @Override
    public void update(Wish wish) {
        wishJpaRepository.save(
                WishJpaMapper.INSTANCE.toWishEntity(wish)
        );
    }

    @Override
    public void delete(Integer id) {
        wishJpaRepository.deleteById(id);
    }

    @Override
    public long count() {
        return wishJpaRepository.count();
    }

    @Override
    public Wish save(Wish wish) {
        WishEntity wishEntity =
                WishJpaMapper.INSTANCE.toWishEntity(wish);

        WishEntity savedWish =
                wishJpaRepository.save(wishEntity);

        return WishJpaMapper.INSTANCE.toWish(savedWish);
    }

    @Override
    public List<Wish> findByUserId(Integer userId) {
        return wishJpaRepository
                .findAllByUser_Id(userId)
                .stream()
                .map(WishJpaMapper.INSTANCE::toWish)
                .toList();
    }

    @Override
    public List<Wish> findByEventId(Integer eventId) {
        return wishJpaRepository
                .findAllByEvent_Id(eventId)
                .stream()
                .map(WishJpaMapper.INSTANCE::toWish)
                .toList();
    }

    @Override
    public List<Wish> findByUserIdAndEventId(
            Integer userId,
            Integer eventId
    ) {
        return wishJpaRepository
                .findAllByUser_IdAndEvent_Id(userId, eventId)
                .stream()
                .map(WishJpaMapper.INSTANCE::toWish)
                .toList();
    }

    @Override
    public Optional<Wish> findByIdAndUserId(
            Integer wishId,
            Integer userId
    ) {
        return wishJpaRepository
                .findByIdAndUser_Id(wishId, userId)
                .map(WishJpaMapper.INSTANCE::toWish);
    }

    @Override
    public boolean existsByIdAndUserId(
            Integer wishId,
            Integer userId
    ) {
        return wishJpaRepository
                .existsByIdAndUser_Id(wishId, userId);
    }

    @Override
    public void deleteByIdAndUserId(
            Integer wishId,
            Integer userId
    ) {
        wishJpaRepository
                .deleteByIdAndUser_Id(wishId, userId);
    }
}