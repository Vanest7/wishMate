package com.gifts.demo.controller;

import com.gifts.demo.controller.exception.ApiExceptionHandler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import com.gifts.demo.controller.mapper.GiftReservationWebMapper;
import com.gifts.demo.controller.webmodel.reservation.GiftReservationResponse;
import com.gifts.demo.domain.model.GiftReservation;
import com.gifts.demo.domain.usecase.reservation.CancelGiftReservationUseCase;
import com.gifts.demo.domain.usecase.reservation.CreateGiftReservationUseCase;
import com.gifts.demo.domain.usecase.reservation.GetGiftReservationUseCase;
import com.gifts.demo.domain.usecase.reservation.GetMyGiftReservationsUseCase;
import com.gifts.demo.security.currentUser.CurrentUserProvider;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Gift reservations")
@RestController
@RequestMapping("/api/reservations")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class GiftReservationController {

    private final CreateGiftReservationUseCase createGiftReservationUseCase;
    private final GetMyGiftReservationsUseCase getMyGiftReservationsUseCase;
    private final GetGiftReservationUseCase getGiftReservationUseCase;
    private final CancelGiftReservationUseCase cancelGiftReservationUseCase;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "Reserve a wish",
            description = "Requires an ACTIVE event and group membership. Users cannot reserve their own wishes. An existing reservation produces HTTP 409.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Reservation created",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Location", description = "Current reservation creation request URI. This is not the GET /api/reservations/{reservationId} URI.",
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
    @PostMapping("/groups/{groupId}/events/{eventId}/wishes/{wishId}")
    public ResponseEntity<GiftReservationResponse> createReservation(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("eventId") Integer eventId,
            @PathVariable("wishId") Integer wishId
    ) {
        GiftReservation reservation = createGiftReservationUseCase.execute(
                groupId,
                eventId,
                wishId,
                currentUserProvider.getCurrentUserId()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();

        return ResponseEntity.created(location)
                .body(GiftReservationWebMapper.toResponse(reservation));
    }

    @Operation(summary = "List my reservations",
            description = "Returns only reservations made by the authenticated user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reservations returned",
                    useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Authentication is missing or the Bearer token is invalid or expired. Rejected by Spring Security; the application ErrorResponse body is not guaranteed.",
                    content = @Content),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<GiftReservationResponse>> getMyReservations() {
        List<GiftReservation> reservations =
                getMyGiftReservationsUseCase.execute(
                        currentUserProvider.getCurrentUserId()
                );

        return ResponseEntity.ok(
                GiftReservationWebMapper.toResponseList(reservations)
        );
    }

    @Operation(summary = "Get my reservation",
            description = "Only the user who made the reservation can read it.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reservation returned",
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
    @GetMapping("/{reservationId}")
    public ResponseEntity<GiftReservationResponse> getReservation(
            @PathVariable("reservationId") Integer reservationId
    ) {
        GiftReservation reservation = getGiftReservationUseCase.execute(
                reservationId,
                currentUserProvider.getCurrentUserId()
        );

        return ResponseEntity.ok(
                GiftReservationWebMapper.toResponse(reservation)
        );
    }

    @Operation(summary = "Cancel my reservation",
            description = "Requires an ACTIVE event and group membership. A missing reservation owned by the requester produces HTTP 403, protecting reservation privacy.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Reservation cancelled",
                    content = @Content),
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
    @DeleteMapping("/groups/{groupId}/events/{eventId}/wishes/{wishId}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("eventId") Integer eventId,
            @PathVariable("wishId") Integer wishId
    ) {
        cancelGiftReservationUseCase.execute(
                groupId,
                eventId,
                wishId,
                currentUserProvider.getCurrentUserId()
        );

        return ResponseEntity.noContent().build();
    }
}