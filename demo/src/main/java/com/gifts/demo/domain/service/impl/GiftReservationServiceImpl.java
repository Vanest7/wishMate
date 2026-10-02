package com.gifts.demo.domain.service.impl;

import com.gifts.demo.common.annotation.DomainService;
import com.gifts.demo.common.annotation.DomainTransactional;
import com.gifts.demo.common.exception.ForbiddenOperationException;
import com.gifts.demo.common.exception.ResourceAlreadyExistsException;
import com.gifts.demo.common.exception.ResourceNotFoundException;
import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.model.GiftReservation;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.domain.model.WishReservationStatus;
import com.gifts.demo.domain.repository.EventRepository;
import com.gifts.demo.domain.repository.GiftReservationRepository;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.repository.WishRepository;
import com.gifts.demo.domain.service.EventStateService;
import com.gifts.demo.domain.service.GiftReservationService;
import com.gifts.demo.domain.service.GroupPermissionService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@DomainService
@RequiredArgsConstructor
public class GiftReservationServiceImpl
        implements GiftReservationService {

    private final GiftReservationRepository giftReservationRepository;
    private final WishRepository wishRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final GroupPermissionService groupPermissionService;
    private final EventStateService eventStateService;

    @Override
    public GiftReservation getById(
            Integer reservationId,
            Integer requesterId
    ) {
        GiftReservation reservation =
                giftReservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reservation with id "
                                                + reservationId
                                )
                        );

        if (reservation.getUser() == null
                || !requesterId.equals(
                reservation.getUser().getId()
        )) {
            throw new ForbiddenOperationException(
                    "User " + requesterId
                            + " cannot access reservation "
                            + reservationId
            );
        }

        return reservation;
    }

    @Override
    public List<GiftReservation> getMyReservations(Integer requesterId) {
        if (requesterId == null) {
            throw new ValidationException(
                    "Requester id cannot be null"
            );
        }

        return giftReservationRepository.findByUserId(requesterId);
    }

    @Override
    @DomainTransactional
    public GiftReservation reserve(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    ) {
        Event event = getEvent(groupId, eventId);

        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        eventStateService.requireReservationAllowed(event);

        Wish wish = getWish(eventId, wishId);

        requireNotWishOwner(wish, requesterId);

        if (giftReservationRepository.existsByWishId(wishId)) {
            throw new ResourceAlreadyExistsException(
                    "Wish " + wishId + " is already reserved"
            );
        }

        User requester = getUser(requesterId);

        GiftReservation reservation = GiftReservation.builder()
                .user(requester)
                .wish(wish)
                .build();

        return giftReservationRepository.save(reservation);
    }

    @Override
    @DomainTransactional
    public void release(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    ) {
        Event event = getEvent(groupId, eventId);

        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        eventStateService.requireReservationAllowed(event);

        Wish wish = getWish(eventId, wishId);

        /*
         * This check must happen before querying the reservation.
         * The wish owner must not learn whether a reservation exists.
         */
        requireNotWishOwner(wish, requesterId);

        GiftReservation reservation =
                giftReservationRepository
                        .findByWishIdAndUserId(
                                wishId,
                                requesterId
                        )
                        .orElseThrow(() ->
                                new ForbiddenOperationException(
                                        "The current user does not own "
                                                + "a reservation for wish "
                                                + wishId
                                )
                        );

        giftReservationRepository.delete(
                reservation.getId()
        );
    }

    @Override
    public Optional<WishReservationStatus> getVisibleStatus(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    ) {
        getEvent(groupId, eventId);

        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        Wish wish = getWish(eventId, wishId);

        /*
         * Critical privacy rule:
         * do not even query reservation data for the wish owner.
         */
        if (isWishOwner(wish, requesterId)) {
            return Optional.empty();
        }

        Optional<GiftReservation> reservation =
                giftReservationRepository.findByWishId(wishId);

        if (reservation.isEmpty()) {
            return Optional.of(
                    WishReservationStatus.AVAILABLE
            );
        }

        boolean reservedByRequester =
                requesterId.equals(
                        reservation.get()
                                .getUser()
                                .getId()
                );

        if (reservedByRequester) {
            return Optional.of(
                    WishReservationStatus.RESERVED_BY_ME
            );
        }

        return Optional.of(
                WishReservationStatus.RESERVED
        );
    }

    private Event getEvent(
            Integer groupId,
            Integer eventId
    ) {
        return eventRepository
                .findByIdAndGroupId(eventId, groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event " + eventId
                                        + " not found in group "
                                        + groupId
                        )
                );
    }

    private Wish getWish(
            Integer eventId,
            Integer wishId
    ) {
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Wish with id " + wishId
                        )
                );

        if (wish.getEvent() == null
                || !eventId.equals(
                wish.getEvent().getId()
        )) {
            throw new ResourceNotFoundException(
                    "Wish " + wishId
                            + " not found in event "
                            + eventId
            );
        }

        return wish;
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with id " + userId
                        )
                );
    }

    private void requireNotWishOwner(
            Wish wish,
            Integer userId
    ) {
        if (isWishOwner(wish, userId)) {
            throw new ForbiddenOperationException(
                    "A user cannot reserve their own wish"
            );
        }
    }

    private boolean isWishOwner(
            Wish wish,
            Integer userId
    ) {
        return userId.equals(
                wish.getUser().getId()
        );
    }
}