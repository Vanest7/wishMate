package com.gifts.demo.controller;

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