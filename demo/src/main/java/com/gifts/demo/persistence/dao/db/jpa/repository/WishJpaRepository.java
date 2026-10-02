package com.gifts.demo.persistence.dao.db.jpa.repository;

import com.gifts.demo.persistence.dao.db.jpa.entity.WishEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishJpaRepository
        extends JpaRepository<WishEntity, Integer> {

    List<WishEntity> findAllByUser_Id(Integer userId);

    List<WishEntity> findAllByEvent_Id(Integer eventId);

    List<WishEntity> findAllByUser_IdAndEvent_Id(
            Integer userId,
            Integer eventId
    );

    Optional<WishEntity> findByIdAndUser_Id(
            Integer wishId,
            Integer userId
    );

    boolean existsByIdAndUser_Id(
            Integer wishId,
            Integer userId
    );

    void deleteByIdAndUser_Id(
            Integer wishId,
            Integer userId
    );
}