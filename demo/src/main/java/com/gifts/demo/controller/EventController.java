package com.gifts.demo.controller;

import com.gifts.demo.controller.exception.ApiExceptionHandler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


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

@Tag(name = "Events")
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

    @Operation(summary = "Create an event",
            description = "Creates an event within the specified group.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Event created",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Location", description = "Created resource URI.",
                            schema = @Schema(type = "string", format = "uri"))),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Authentication is missing or the Bearer token is invalid or expired. Rejected by Spring Security; the application ErrorResponse body is not guaranteed.",
                    content = @Content),
        @ApiResponse(responseCode = "403",
                    description = "Access is denied or the CSRF token/cookie is missing or invalid. Application permission failures return ErrorResponse; Spring Security failures may have a different body.",
                    content = @Content),
        @ApiResponse(responseCode = "404",
                    description = "A resource required by this operation was not found. Permission checks may produce HTTP 403 first.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409",
                    description = "The operation conflicts with stored data or a domain constraint. See code and message for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "List group events",
            description = "Returns an array of events for the specified group.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Events returned",
                    useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Authentication is missing or the Bearer token is invalid or expired. Rejected by Spring Security; the application ErrorResponse body is not guaranteed.",
                    content = @Content),
        @ApiResponse(responseCode = "403",
                    description = "Access is denied or the CSRF token/cookie is missing or invalid. Application permission failures return ErrorResponse; Spring Security failures may have a different body.",
                    content = @Content),
        @ApiResponse(responseCode = "404",
                    description = "A resource required by this operation was not found. Permission checks may produce HTTP 403 first.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Get an event",
            description = "The event must belong to the specified group.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event returned",
                    useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Authentication is missing or the Bearer token is invalid or expired. Rejected by Spring Security; the application ErrorResponse body is not guaranteed.",
                    content = @Content),
        @ApiResponse(responseCode = "403",
                    description = "Access is denied or the CSRF token/cookie is missing or invalid. Application permission failures return ErrorResponse; Spring Security failures may have a different body.",
                    content = @Content),
        @ApiResponse(responseCode = "404",
                    description = "A resource required by this operation was not found. Permission checks may produce HTTP 403 first.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Update an event",
            description = "Closed or archived events cannot be modified; invalid state produces HTTP 400.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event updated",
                    useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Authentication is missing or the Bearer token is invalid or expired. Rejected by Spring Security; the application ErrorResponse body is not guaranteed.",
                    content = @Content),
        @ApiResponse(responseCode = "403",
                    description = "Access is denied or the CSRF token/cookie is missing or invalid. Application permission failures return ErrorResponse; Spring Security failures may have a different body.",
                    content = @Content),
        @ApiResponse(responseCode = "404",
                    description = "A resource required by this operation was not found. Permission checks may produce HTTP 403 first.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409",
                    description = "The operation conflicts with stored data or a domain constraint. See code and message for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Activate an event",
            description = "Only DRAFT events can be activated. Invalid state produces HTTP 400.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event activated",
                    useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Authentication is missing or the Bearer token is invalid or expired. Rejected by Spring Security; the application ErrorResponse body is not guaranteed.",
                    content = @Content),
        @ApiResponse(responseCode = "403",
                    description = "Access is denied or the CSRF token/cookie is missing or invalid. Application permission failures return ErrorResponse; Spring Security failures may have a different body.",
                    content = @Content),
        @ApiResponse(responseCode = "404",
                    description = "A resource required by this operation was not found. Permission checks may produce HTTP 403 first.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409",
                    description = "The operation conflicts with stored data or a domain constraint. See code and message for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Close an event",
            description = "Only ACTIVE events can be closed. Invalid state produces HTTP 400.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event closed",
                    useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Authentication is missing or the Bearer token is invalid or expired. Rejected by Spring Security; the application ErrorResponse body is not guaranteed.",
                    content = @Content),
        @ApiResponse(responseCode = "403",
                    description = "Access is denied or the CSRF token/cookie is missing or invalid. Application permission failures return ErrorResponse; Spring Security failures may have a different body.",
                    content = @Content),
        @ApiResponse(responseCode = "404",
                    description = "A resource required by this operation was not found. Permission checks may produce HTTP 403 first.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409",
                    description = "The operation conflicts with stored data or a domain constraint. See code and message for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Archive an event",
            description = "Only CLOSED events can be archived. Invalid state produces HTTP 400.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event archived",
                    useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Authentication is missing or the Bearer token is invalid or expired. Rejected by Spring Security; the application ErrorResponse body is not guaranteed.",
                    content = @Content),
        @ApiResponse(responseCode = "403",
                    description = "Access is denied or the CSRF token/cookie is missing or invalid. Application permission failures return ErrorResponse; Spring Security failures may have a different body.",
                    content = @Content),
        @ApiResponse(responseCode = "404",
                    description = "A resource required by this operation was not found. Permission checks may produce HTTP 403 first.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409",
                    description = "The operation conflicts with stored data or a domain constraint. See code and message for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
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