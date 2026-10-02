package com.gifts.demo.domain.service.impl;

import com.gifts.demo.common.annotation.DomainService;
import com.gifts.demo.common.annotation.DomainTransactional;
import com.gifts.demo.common.exception.ResourceNotFoundException;
import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.model.EventStatus;
import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.repository.EventRepository;
import com.gifts.demo.domain.repository.GroupRepository;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.service.EventService;
import com.gifts.demo.domain.service.EventStateService;
import com.gifts.demo.domain.service.GroupPermissionService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@DomainService
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupPermissionService groupPermissionService;
    private final EventStateService eventStateService;

    @Override
    public Event getById(
            Integer groupId,
            Integer eventId,
            Integer requesterId
    ) {
        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        return getEvent(groupId, eventId);
    }

    @Override
    public List<Event> getByGroupId(
            Integer groupId,
            Integer requesterId
    ) {
        getGroup(groupId);

        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        return eventRepository.findByGroupId(groupId);
    }

    @Override
    @DomainTransactional
    public Event create(
            Integer groupId,
            Integer requesterId,
            Event event
    ) {
        validateEvent(event);

        Group group = getGroup(groupId);

        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        User creator = getUser(requesterId);

        EventStatus initialStatus =
                event.getStatus() == null
                        ? EventStatus.ACTIVE
                        : event.getStatus();

        if (initialStatus != EventStatus.DRAFT
                && initialStatus != EventStatus.ACTIVE) {
            throw new ValidationException(
                    "A new event must be draft or active"
            );
        }

        event.setId(null);
        event.setGroup(group);
        event.setCreator(creator);
        event.setStatus(initialStatus);
        event.setCreatedAt(null);
        event.setUpdatedAt(null);

        return eventRepository.save(event);
    }

    @Override
    @DomainTransactional
    public Event update(
            Integer groupId,
            Integer eventId,
            Integer requesterId,
            Event event
    ) {
        validateEvent(event);

        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        Event currentEvent = getEvent(groupId, eventId);

        eventStateService.requireEventEditable(currentEvent);

        currentEvent.setName(event.getName());
        currentEvent.setDescription(event.getDescription());
        currentEvent.setStartDate(event.getStartDate());
        currentEvent.setEndDate(event.getEndDate());

        return eventRepository.save(currentEvent);
    }

    @Override
    @DomainTransactional
    public Event activate(
            Integer groupId,
            Integer eventId,
            Integer requesterId
    ) {
        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        Event event = getEvent(groupId, eventId);

        eventStateService.validateActivation(event);

        event.setStatus(EventStatus.ACTIVE);

        return eventRepository.save(event);
    }

    @Override
    @DomainTransactional
    public Event close(
            Integer groupId,
            Integer eventId,
            Integer requesterId
    ) {
        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        Event event = getEvent(groupId, eventId);

        eventStateService.validateClosing(event);

        event.setStatus(EventStatus.CLOSED);

        return eventRepository.save(event);
    }

    @Override
    @DomainTransactional
    public Event archive(
            Integer groupId,
            Integer eventId,
            Integer requesterId
    ) {
        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        Event event = getEvent(groupId, eventId);

        eventStateService.validateArchiving(event);

        event.setStatus(EventStatus.ARCHIVED);

        return eventRepository.save(event);
    }

    private Event getEvent(
            Integer groupId,
            Integer eventId
    ) {
        return eventRepository
                .findByIdAndGroupId(eventId, groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event " + eventId
                                        + " not found in group "
                                        + groupId
                        )
                );
    }

    private Group getGroup(Integer groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group with id " + groupId
                        )
                );
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with id " + userId
                        )
                );
    }

    private void validateEvent(Event event) {
        if (event == null) {
            throw new ValidationException(
                    "Event cannot be null"
            );
        }

        if (event.getName() == null
                || event.getName().isBlank()) {
            throw new ValidationException(
                    "Event name cannot be empty"
            );
        }

        if (event.getName().length() > 100) {
            throw new ValidationException(
                    "Event name must not exceed 100 characters"
            );
        }

        if (event.getDescription() != null
                && event.getDescription().length() > 255) {
            throw new ValidationException(
                    "Event description must not exceed 255 characters"
            );
        }

        validateDates(
                event.getStartDate(),
                event.getEndDate()
        );

        event.setName(event.getName().strip());
    }

    private void validateDates(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate != null
                && endDate != null
                && endDate.isBefore(startDate)) {
            throw new ValidationException(
                    "Event end date cannot be before start date"
            );
        }
    }
}