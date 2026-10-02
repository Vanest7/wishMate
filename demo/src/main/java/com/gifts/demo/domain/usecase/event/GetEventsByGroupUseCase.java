package com.gifts.demo.domain.usecase.event;

import com.gifts.demo.domain.model.Event;

import java.util.List;

public interface GetEventsByGroupUseCase {

    List<Event> execute(
            Integer groupId,
            Integer requesterId
    );
}