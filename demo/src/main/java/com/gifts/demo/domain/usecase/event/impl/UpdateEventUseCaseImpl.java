package com.gifts.demo.domain.usecase.event.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.service.EventService;
import com.gifts.demo.domain.usecase.event.UpdateEventUseCase;
import lombok.RequiredArgsConstructor;

@DomainUseCase
@RequiredArgsConstructor
public class UpdateEventUseCaseImpl implements UpdateEventUseCase {

    private final EventService eventService;

    @Override
    public Event execute(
            Integer groupId,
            Integer eventId,
            Integer requesterId,
            Event event
    ) {
        return eventService.update(
                groupId,
                eventId,
                requesterId,
                event
        );
    }
}