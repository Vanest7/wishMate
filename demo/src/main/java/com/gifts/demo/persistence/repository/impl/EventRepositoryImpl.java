package com.gifts.demo.persistence.repository.impl;

import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.model.EventStatus;
import com.gifts.demo.domain.repository.EventRepository;
import com.gifts.demo.persistence.dao.db.EventDaoDb;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {

    private final EventDaoDb eventDaoDb;

    @Override
    public Optional<Event> findById(Integer id) {
        return eventDaoDb.findById(id);
    }

    @Override
    public List<Event> findByGroupId(Integer groupId) {
        return eventDaoDb.findByGroupId(groupId);
    }

    @Override
    public List<Event> findByGroupIdAndStatus(
            Integer groupId,
            EventStatus status
    ) {
        return eventDaoDb.findByGroupIdAndStatus(
                groupId,
                status
        );
    }

    @Override
    public Optional<Event> findByIdAndGroupId(
            Integer eventId,
            Integer groupId
    ) {
        return eventDaoDb.findByIdAndGroupId(
                eventId,
                groupId
        );
    }

    @Override
    public boolean existsByIdAndGroupId(
            Integer eventId,
            Integer groupId
    ) {
        return eventDaoDb.existsByIdAndGroupId(
                eventId,
                groupId
        );
    }

    @Override
    public Event save(Event event) {
        return eventDaoDb.save(event);
    }

    @Override
    public void delete(Integer id) {
        eventDaoDb.delete(id);
    }
}