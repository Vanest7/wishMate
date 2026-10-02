package com.gifts.demo.domain.usecase.event;

import com.gifts.demo.domain.model.Event;

public interface CreateEventUseCase {

    Event execute(
            Integer groupId,
            Integer requesterId,
            Event event
    );
}