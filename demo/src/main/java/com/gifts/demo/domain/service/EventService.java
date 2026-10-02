package com.gifts.demo.domain.service;

import com.gifts.demo.domain.model.Event;

import java.util.List;

public interface EventService {

    Event getById(
            Integer groupId,
            Integer eventId,
            Integer requesterId
    );

    List<Event> getByGroupId(
            Integer groupId,
            Integer requesterId
    );

    Event create(
            Integer groupId,
            Integer requesterId,
            Event event
    );

    Event update(
            Integer groupId,
            Integer eventId,
            Integer requesterId,
            Event event
    );

    Event activate(
            Integer groupId,
            Integer eventId,
            Integer requesterId
    );

    Event close(
            Integer groupId,
            Integer eventId,
            Integer requesterId
    );

    Event archive(
            Integer groupId,
            Integer eventId,
            Integer requesterId
    );
}