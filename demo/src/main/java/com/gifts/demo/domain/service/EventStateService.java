package com.gifts.demo.domain.service;

import com.gifts.demo.domain.model.Event;

public interface EventStateService {

    void requireEventEditable(Event event);

    void requireWishModificationAllowed(Event event);

    void requireReservationAllowed(Event event);

    void validateActivation(Event event);

    void validateClosing(Event event);

    void validateArchiving(Event event);
}