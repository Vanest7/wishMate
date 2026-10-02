package com.gifts.demo.domain.service.impl;

import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.model.EventStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventStateServiceImplTest {

    private final EventStateServiceImpl service =
            new EventStateServiceImpl();

    @ParameterizedTest
    @EnumSource(EventStatus.class)
    void shouldAllowReservationsOnlyInActiveEvents(EventStatus status) {
        Event event = createEvent(status);

        assertAllowed(
                () -> service.requireReservationAllowed(event),
                status == EventStatus.ACTIVE
        );
    }

    @ParameterizedTest
    @EnumSource(EventStatus.class)
    void shouldAllowWishChangesOnlyInActiveEvents(EventStatus status) {
        Event event = createEvent(status);

        assertAllowed(
                () -> service.requireWishModificationAllowed(event),
                status == EventStatus.ACTIVE
        );
    }

    @ParameterizedTest
    @EnumSource(EventStatus.class)
    void shouldAllowEditingOnlyDraftOrActiveEvents(EventStatus status) {
        Event event = createEvent(status);

        assertAllowed(
                () -> service.requireEventEditable(event),
                status == EventStatus.DRAFT
                        || status == EventStatus.ACTIVE
        );
    }

    @ParameterizedTest
    @EnumSource(EventStatus.class)
    void shouldAllowActivationOnlyFromDraft(EventStatus status) {
        Event event = createEvent(status);

        assertAllowed(
                () -> service.validateActivation(event),
                status == EventStatus.DRAFT
        );
    }

    @ParameterizedTest
    @EnumSource(EventStatus.class)
    void shouldAllowClosingOnlyFromActive(EventStatus status) {
        Event event = createEvent(status);

        assertAllowed(
                () -> service.validateClosing(event),
                status == EventStatus.ACTIVE
        );
    }

    @ParameterizedTest
    @EnumSource(EventStatus.class)
    void shouldAllowArchivingOnlyFromClosed(EventStatus status) {
        Event event = createEvent(status);

        assertAllowed(
                () -> service.validateArchiving(event),
                status == EventStatus.CLOSED
        );
    }

    private Event createEvent(EventStatus status) {
        return Event.builder()
                .id(4)
                .status(status)
                .build();
    }

    private void assertAllowed(Runnable operation, boolean allowed) {
        if (allowed) {
            assertThatCode(operation::run)
                    .doesNotThrowAnyException();
        } else {
            assertThatThrownBy(operation::run)
                    .isInstanceOf(ValidationException.class);
        }
    }
}