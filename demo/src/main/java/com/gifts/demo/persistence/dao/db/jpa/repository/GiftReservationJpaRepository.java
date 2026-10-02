package com.gifts.demo.persistence.dao.db.jpa.repository;

import com.gifts.demo.persistence.dao.db.jpa.entity.GiftReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GiftReservationJpaRepository
        extends JpaRepository<GiftReservationEntity, Integer> {

    Optional<GiftReservationEntity> findByWish_Id(Integer wishId);

    boolean existsByWish_Id(Integer wishId);

    Optional<GiftReservationEntity> findByWish_IdAndUser_Id(
            Integer wishId,
            Integer userId
    );
}