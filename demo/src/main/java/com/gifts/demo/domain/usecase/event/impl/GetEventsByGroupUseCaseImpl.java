package com.gifts.demo.domain.usecase.event.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.service.EventService;
import com.gifts.demo.domain.usecase.event.GetEventsByGroupUseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@DomainUseCase
@RequiredArgsConstructor
public class GetEventsByGroupUseCaseImpl implements GetEventsByGroupUseCase {

    private final EventService eventService;

    @Override
    public List<Event> execute(
            Integer groupId,
            Integer requesterId
    ) {
        return eventService.getByGroupId(
                groupId,
                requesterId
        );
    }
}