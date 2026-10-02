package com.gifts.demo.domain.usecase.event;

import com.gifts.demo.domain.model.Event;

public interface ActivateEventUseCase {

    Event execute(
            Integer groupId,
            Integer eventId,
            Integer requesterId
    );
}