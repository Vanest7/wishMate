package com.gifts.demo.persistence.dao.db.jpa;

import com.gifts.demo.domain.model.GiftReservation;
import com.gifts.demo.domain.model.ListWithCount;
import com.gifts.demo.persistence.dao.db.GiftReservationDaoDb;
import com.gifts.demo.persistence.dao.db.jpa.entity.GiftReservationEntity;
import com.gifts.demo.persistence.dao.db.jpa.mapper.GiftReservationJpaMapper;
import com.gifts.demo.persistence.dao.db.jpa.repository.GiftReservationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Primary
@RequiredArgsConstructor
public class GiftReservationDaoJpa
        implements GiftReservationDaoDb {

    private final GiftReservationJpaRepository giftReservationJpaRepository;

    @Override
    public List<GiftReservation> getAll() {
        return giftReservationJpaRepository
                .findAll()
                .stream()
                .map(GiftReservationJpaMapper.INSTANCE::toGiftReservation)
                .toList();
    }

    @Override
    public ListWithCount<GiftReservation> getAll(
            int page,
            int size
    ) {
        Page<GiftReservationEntity> reservationPage =
                giftReservationJpaRepository.findAll(
                        PageRequest.of(page, size)
                );

        return new ListWithCount<>(
                reservationPage.stream()
                        .map(GiftReservationJpaMapper.INSTANCE::toGiftReservation)
                        .toList(),
                reservationPage.getTotalElements()
        );
    }

    @Override
    public Optional<GiftReservation> findById(Integer id) {
        return giftReservationJpaRepository
                .findById(id)
                .map(GiftReservationJpaMapper.INSTANCE::toGiftReservation);
    }

    @Override
    public Integer insert(GiftReservation giftReservation) {
        GiftReservationEntity entity =
                GiftReservationJpaMapper.INSTANCE
                        .toGiftReservationEntity(giftReservation);

        return giftReservationJpaRepository
                .save(entity)
                .getId();
    }

    @Override
    public void update(GiftReservation giftReservation) {
        giftReservationJpaRepository.save(
                GiftReservationJpaMapper.INSTANCE
                        .toGiftReservationEntity(giftReservation)
        );
    }

    @Override
    public void delete(Integer id) {
        giftReservationJpaRepository.deleteById(id);
    }

    @Override
    public long count() {
        return giftReservationJpaRepository.count();
    }

    @Override
    public GiftReservation save(
            GiftReservation giftReservation
    ) {
        GiftReservationEntity entity =
                GiftReservationJpaMapper.INSTANCE
                        .toGiftReservationEntity(
                                giftReservation
                        );

        GiftReservationEntity savedEntity =
                giftReservationJpaRepository
                        .saveAndFlush(entity);

        return GiftReservationJpaMapper.INSTANCE
                .toGiftReservation(savedEntity);
    }

    @Override
    public Optional<GiftReservation> findByWishId(
            Integer wishId
    ) {
        return giftReservationJpaRepository
                .findByWish_Id(wishId)
                .map(GiftReservationJpaMapper.INSTANCE::toGiftReservation);
    }

    @Override
    public List<GiftReservation> findByUserId(Integer userId) {
        return giftReservationJpaRepository
                .findByUser_Id(userId)
                .stream()
                .map(GiftReservationJpaMapper.INSTANCE::toGiftReservation)
                .toList();
    }

    @Override
    public boolean existsByWishId(Integer wishId) {
        return giftReservationJpaRepository
                .existsByWish_Id(wishId);
    }

    @Override
    public Optional<GiftReservation> findByWishIdAndUserId(
            Integer wishId,
            Integer userId
    ) {
        return giftReservationJpaRepository
                .findByWish_IdAndUser_Id(wishId, userId)
                .map(GiftReservationJpaMapper.INSTANCE::toGiftReservation);
    }
}