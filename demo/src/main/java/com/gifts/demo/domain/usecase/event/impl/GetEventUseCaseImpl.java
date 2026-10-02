package com.gifts.demo.domain.usecase.event.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.service.EventService;
import com.gifts.demo.domain.usecase.event.GetEventUseCase;
import lombok.RequiredArgsConstructor;

@DomainUseCase
@RequiredArgsConstructor
public class GetEventUseCaseImpl implements GetEventUseCase {

    private final EventService eventService;

    @Override
    public Event execute(
            Integer groupId,
            Integer eventId,
            Integer requesterId
    ) {
        return eventService.getById(
                groupId,
                eventId,
                requesterId
        );
    }
}