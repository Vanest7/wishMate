package com.gifts.demo.domain.usecase.event.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.service.EventService;
import com.gifts.demo.domain.usecase.event.CreateEventUseCase;
import lombok.RequiredArgsConstructor;

@DomainUseCase
@RequiredArgsConstructor
public class CreateEventUseCaseImpl implements CreateEventUseCase {

    private final EventService eventService;

    @Override
    public Event execute(
            Integer groupId,
            Integer requesterId,
            Event event
    ) {
        return eventService.create(
                groupId,
                requesterId,
                event
        );
    }
}