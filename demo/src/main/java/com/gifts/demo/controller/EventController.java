package com.gifts.demo.controller;

import com.gifts.demo.controller.mapper.EventWebMapper;
import com.gifts.demo.controller.webmodel.event.CreateEventRequest;
import com.gifts.demo.controller.webmodel.event.EventResponse;
import com.gifts.demo.controller.webmodel.event.UpdateEventRequest;
import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.usecase.event.ActivateEventUseCase;
import com.gifts.demo.domain.usecase.event.ArchiveEventUseCase;
import com.gifts.demo.domain.usecase.event.CloseEventUseCase;
import com.gifts.demo.domain.usecase.event.CreateEventUseCase;
import com.gifts.demo.domain.usecase.event.GetEventsByGroupUseCase;
import com.gifts.demo.domain.usecase.event.GetEventUseCase;
import com.gifts.demo.domain.usecase.event.UpdateEventUseCase;
import com.gifts.demo.security.currentUser.CurrentUserProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/events")
@RequiredArgsConstructor
public class EventController {

    private final CreateEventUseCase createEventUseCase;
    private final GetEventsByGroupUseCase getEventsByGroupUseCase;
    private final GetEventUseCase getEventUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final ActivateEventUseCase activateEventUseCase;
    private final CloseEventUseCase closeEventUseCase;
    private final ArchiveEventUseCase archiveEventUseCase;

    private final CurrentUserProvider currentUserProvider;
    private final EventWebMapper eventWebMapper;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @PathVariable("groupId") Integer groupId,
            @Valid @RequestBody CreateEventRequest request
    ) {
        Event event = createEventUseCase.execute(
                groupId,
                currentUserProvider.getCurrentUserId(),
                eventWebMapper.toEvent(request)
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{eventId}")
                .buildAndExpand(event.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(eventWebMapper.toEventResponse(event));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getEvents(
            @PathVariable("groupId") Integer groupId
    ) {
        List<EventResponse> events = getEventsByGroupUseCase
                .execute(
                        groupId,
                        currentUserProvider.getCurrentUserId()
                )
                .stream()
                .map(eventWebMapper::toEventResponse)
                .toList();

        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEvent(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("eventId") Integer eventId
    ) {
        Event event = getEventUseCase.execute(
                groupId,
                eventId,
                currentUserProvider.getCurrentUserId()
        );

        return ResponseEntity.ok(
                eventWebMapper.toEventResponse(event)
        );
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("eventId") Integer eventId,
            @Valid @RequestBody UpdateEventRequest request
    ) {
        Event event = updateEventUseCase.execute(
                groupId,
                eventId,
                currentUserProvider.getCurrentUserId(),
                eventWebMapper.toEvent(request)
        );

        return ResponseEntity.ok(
                eventWebMapper.toEventResponse(event)
        );
    }

    @PostMapping("/{eventId}/activate")
    public ResponseEntity<EventResponse> activateEvent(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("eventId") Integer eventId
    ) {
        Event event = activateEventUseCase.execute(
                groupId,
                eventId,
                currentUserProvider.getCurrentUserId()
        );

        return ResponseEntity.ok(
                eventWebMapper.toEventResponse(event)
        );
    }

    @PostMapping("/{eventId}/close")
    public ResponseEntity<EventResponse> closeEvent(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("eventId") Integer eventId
    ) {
        Event event = closeEventUseCase.execute(
                groupId,
                eventId,
                currentUserProvider.getCurrentUserId()
        );

        return ResponseEntity.ok(
                eventWebMapper.toEventResponse(event)
        );
    }

    @PostMapping("/{eventId}/archive")
    public ResponseEntity<EventResponse> archiveEvent(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("eventId") Integer eventId
    ) {
        Event event = archiveEventUseCase.execute(
                groupId,
                eventId,
                currentUserProvider.getCurrentUserId()
        );

        return ResponseEntity.ok(
                eventWebMapper.toEventResponse(event)
        );
    }
}