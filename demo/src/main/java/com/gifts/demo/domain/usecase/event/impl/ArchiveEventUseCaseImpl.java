package com.gifts.demo.domain.usecase.event.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.service.EventService;
import com.gifts.demo.domain.usecase.event.ArchiveEventUseCase;
import lombok.RequiredArgsConstructor;

@DomainUseCase
@RequiredArgsConstructor
public class ArchiveEventUseCaseImpl implements ArchiveEventUseCase {

    private final EventService eventService;

    @Override
    public Event execute(
            Integer groupId,
            Integer eventId,
            Integer requesterId
    ) {
        return eventService.archive(
                groupId,
                eventId,
                requesterId
        );
    }
}