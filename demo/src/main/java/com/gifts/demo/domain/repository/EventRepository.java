package com.gifts.demo.domain.repository;

import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.model.EventStatus;

import java.util.List;
import java.util.Optional;

public interface EventRepository {

    Optional<Event> findById(Integer id);

    List<Event> findByGroupId(Integer groupId);

    List<Event> findByGroupIdAndStatus(
            Integer groupId,
            EventStatus status
    );

    Optional<Event> findByIdAndGroupId(
            Integer eventId,
            Integer groupId
    );

    boolean existsByIdAndGroupId(
            Integer eventId,
            Integer groupId
    );

    Event save(Event event);

    void delete(Integer id);
}