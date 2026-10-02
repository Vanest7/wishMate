package com.gifts.demo.persistence.dao.db.jpa.repository;

import com.gifts.demo.domain.model.EventStatus;
import com.gifts.demo.persistence.dao.db.jpa.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventJpaRepository
        extends JpaRepository<EventEntity, Integer> {

    List<EventEntity> findAllByGroup_Id(Integer groupId);

    List<EventEntity> findAllByGroup_IdAndStatus(
            Integer groupId,
            EventStatus status
    );

    Optional<EventEntity> findByIdAndGroup_Id(
            Integer eventId,
            Integer groupId
    );

    boolean existsByIdAndGroup_Id(
            Integer eventId,
            Integer groupId
    );
}