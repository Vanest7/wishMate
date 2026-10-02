package com.gifts.demo.persistence.dao.db.jpa;

import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.model.EventStatus;
import com.gifts.demo.domain.model.ListWithCount;
import com.gifts.demo.persistence.dao.db.EventDaoDb;
import com.gifts.demo.persistence.dao.db.jpa.entity.EventEntity;
import com.gifts.demo.persistence.dao.db.jpa.mapper.EventJpaMapper;
import com.gifts.demo.persistence.dao.db.jpa.repository.EventJpaRepository;
import jakarta.persistence.EntityManager;
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
public class EventDaoJpa implements EventDaoDb {

    private final EventJpaRepository eventJpaRepository;
    private final EntityManager entityManager;

    @Override
    public List<Event> getAll() {
        return eventJpaRepository
                .findAll()
                .stream()
                .map(EventJpaMapper.INSTANCE::toEvent)
                .toList();
    }

    @Override
    public ListWithCount<Event> getAll(int page, int size) {
        Page<EventEntity> eventPage =
                eventJpaRepository.findAll(PageRequest.of(page, size));

        return new ListWithCount<>(
                eventPage.stream()
                        .map(EventJpaMapper.INSTANCE::toEvent)
                        .toList(),
                eventPage.getTotalElements()
        );
    }

    @Override
    public Optional<Event> findById(Integer id) {
        return eventJpaRepository
                .findById(id)
                .map(EventJpaMapper.INSTANCE::toEvent);
    }

    @Override
    public Integer insert(Event event) {
        EventEntity entity =
                EventJpaMapper.INSTANCE.toEventEntity(event);

        return eventJpaRepository.save(entity).getId();
    }

    @Override
    public void update(Event event) {
        eventJpaRepository.save(
                EventJpaMapper.INSTANCE.toEventEntity(event)
        );
    }

    @Override
    public void delete(Integer id) {
        eventJpaRepository.deleteById(id);
    }

    @Override
    public long count() {
        return eventJpaRepository.count();
    }

    @Override
    public Event save(Event event) {
        EventEntity entity =
                EventJpaMapper.INSTANCE.toEventEntity(event);
        EventEntity savedEntity =
                eventJpaRepository.saveAndFlush(entity);
        entityManager.refresh(savedEntity);
        return EventJpaMapper.INSTANCE.toEvent(savedEntity);
    }

    @Override
    public List<Event> findByGroupId(Integer groupId) {
        return eventJpaRepository
                .findAllByGroup_Id(groupId)
                .stream()
                .map(EventJpaMapper.INSTANCE::toEvent)
                .toList();
    }

    @Override
    public List<Event> findByGroupIdAndStatus(
            Integer groupId,
            EventStatus status
    ) {
        return eventJpaRepository
                .findAllByGroup_IdAndStatus(groupId, status)
                .stream()
                .map(EventJpaMapper.INSTANCE::toEvent)
                .toList();
    }

    @Override
    public Optional<Event> findByIdAndGroupId(
            Integer eventId,
            Integer groupId
    ) {
        return eventJpaRepository
                .findByIdAndGroup_Id(eventId, groupId)
                .map(EventJpaMapper.INSTANCE::toEvent);
    }

    @Override
    public boolean existsByIdAndGroupId(
            Integer eventId,
            Integer groupId
    ) {
        return eventJpaRepository
                .existsByIdAndGroup_Id(eventId, groupId);
    }
}