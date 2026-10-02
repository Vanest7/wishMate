package com.gifts.demo.persistence.dao.db;

import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.model.EventStatus;

import java.util.List;
import java.util.Optional;

public interface EventDaoDb extends GenericDaoDb<Event> {

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
}