package com.gifts.demo.domain.service.impl;

import com.gifts.demo.common.annotation.DomainService;
import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.model.EventStatus;
import com.gifts.demo.domain.service.EventStateService;

@DomainService
public class EventStateServiceImpl implements EventStateService {

    @Override
    public void requireEventEditable(Event event) {
        if (event.getStatus() == EventStatus.CLOSED
                || event.getStatus() == EventStatus.ARCHIVED) {
            throw new ValidationException(
                    "Closed or archived events cannot be modified"
            );
        }
    }

    @Override
    public void requireWishModificationAllowed(Event event) {
        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new ValidationException(
                    "Wishes can only be modified in active events"
            );
        }
    }

    @Override
    public void validateActivation(Event event) {
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new ValidationException(
                    "Only draft events can be activated"
            );
        }
    }

    @Override
    public void validateClosing(Event event) {
        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new ValidationException(
                    "Only active events can be closed"
            );
        }
    }

    @Override
    public void validateArchiving(Event event) {
        if (event.getStatus() != EventStatus.CLOSED) {
            throw new ValidationException(
                    "Only closed events can be archived"
            );
        }
    }

    @Override
    public void requireReservationAllowed(Event event) {
        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new ValidationException(
                    "Gifts can only be reserved in active events"
            );
        }
    }
}