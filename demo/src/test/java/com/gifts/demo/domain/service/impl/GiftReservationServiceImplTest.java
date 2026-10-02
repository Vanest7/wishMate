package com.gifts.demo.domain.service.impl;

import com.gifts.demo.common.exception.ForbiddenOperationException;
import com.gifts.demo.common.exception.ResourceAlreadyExistsException;
import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.model.EventStatus;
import com.gifts.demo.domain.model.GiftReservation;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.domain.repository.EventRepository;
import com.gifts.demo.domain.repository.GiftReservationRepository;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.repository.WishRepository;
import com.gifts.demo.domain.service.EventStateService;
import com.gifts.demo.domain.service.GroupPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiftReservationServiceImplTest {

    private static final Integer GROUP_ID = 1;
    private static final Integer EVENT_ID = 4;
    private static final Integer WISH_ID = 6;
    private static final Integer RESERVATION_ID = 7;

    private static final Integer OWNER_ID = 10;
    private static final Integer REQUESTER_ID = 20;
    private static final Integer OTHER_USER_ID = 30;

    @Mock
    private GiftReservationRepository reservationRepository;

    @Mock
    private WishRepository wishRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupPermissionService groupPermissionService;

    @Mock
    private EventStateService eventStateService;

    private GiftReservationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GiftReservationServiceImpl(
                reservationRepository,
                wishRepository,
                eventRepository,
                userRepository,
                groupPermissionService,
                eventStateService
        );
    }

    @Test
    void shouldReturnSavedReservationIncludingGeneratedDate() {
        prepareEventAndWish();

        User requester = User.builder()
                .id(REQUESTER_ID)
                .build();

        GiftReservation savedReservation = createReservation();

        when(userRepository.findById(REQUESTER_ID))
                .thenReturn(Optional.of(requester));

        when(reservationRepository.save(any(GiftReservation.class)))
                .thenReturn(savedReservation);

        GiftReservation result = service.reserve(
                GROUP_ID,
                EVENT_ID,
                WISH_ID,
                REQUESTER_ID
        );

        assertThat(result).isSameAs(savedReservation);
        assertThat(result.getReservedAt())
                .isEqualTo(savedReservation.getReservedAt());

        verify(groupPermissionService)
                .requireMembership(GROUP_ID, REQUESTER_ID);

        verify(eventStateService)
                .requireReservationAllowed(any(Event.class));

        verify(reservationRepository).save(
                argThat(reservation ->
                        REQUESTER_ID.equals(reservation.getUser().getId())
                                && WISH_ID.equals(reservation.getWish().getId())
                                && reservation.getId() == null
                )
        );
    }

    @Test
    void shouldRejectReservationOfOwnWishWithoutReadingReservations() {
        prepareEventAndWish();

        assertThatThrownBy(() -> service.reserve(
                GROUP_ID,
                EVENT_ID,
                WISH_ID,
                OWNER_ID
        )).isInstanceOf(ForbiddenOperationException.class);

        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldRejectAlreadyReservedWish() {
        prepareEventAndWish();

        when(reservationRepository.existsByWishId(WISH_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> service.reserve(
                GROUP_ID,
                EVENT_ID,
                WISH_ID,
                REQUESTER_ID
        )).isInstanceOf(ResourceAlreadyExistsException.class);

        verify(reservationRepository, never())
                .save(any(GiftReservation.class));
    }

    @Test
    void shouldHideReservationStatusFromWishOwner() {
        prepareEventAndWish();

        assertThat(service.getVisibleStatus(
                GROUP_ID,
                EVENT_ID,
                WISH_ID,
                OWNER_ID
        )).isEmpty();

        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldAllowReserverToReadReservation() {
        GiftReservation reservation = createReservation();

        when(reservationRepository.findById(RESERVATION_ID))
                .thenReturn(Optional.of(reservation));

        GiftReservation result = service.getById(
                RESERVATION_ID,
                REQUESTER_ID
        );

        assertThat(result).isSameAs(reservation);
    }

    @Test
    void shouldPreventWishOwnerFromReadingReservationById() {
        when(reservationRepository.findById(RESERVATION_ID))
                .thenReturn(Optional.of(createReservation()));

        assertThatThrownBy(() -> service.getById(
                RESERVATION_ID,
                OWNER_ID
        )).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void shouldPreventAnotherUserFromReadingReservationById() {
        when(reservationRepository.findById(RESERVATION_ID))
                .thenReturn(Optional.of(createReservation()));

        assertThatThrownBy(() -> service.getById(
                RESERVATION_ID,
                OTHER_USER_ID
        )).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void shouldAllowReserverToCancelReservation() {
        prepareEventAndWish();

        when(reservationRepository.findByWishIdAndUserId(
                WISH_ID,
                REQUESTER_ID
        )).thenReturn(Optional.of(createReservation()));

        service.release(
                GROUP_ID,
                EVENT_ID,
                WISH_ID,
                REQUESTER_ID
        );

        verify(reservationRepository).delete(RESERVATION_ID);
    }

    @Test
    void shouldPreventAnotherUserFromCancellingReservation() {
        prepareEventAndWish();

        when(reservationRepository.findByWishIdAndUserId(
                WISH_ID,
                OTHER_USER_ID
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.release(
                GROUP_ID,
                EVENT_ID,
                WISH_ID,
                OTHER_USER_ID
        )).isInstanceOf(ForbiddenOperationException.class);

        verify(reservationRepository, never()).delete(any());
    }

    @Test
    void shouldPreventOwnerFromProbingReservationThroughCancellation() {
        prepareEventAndWish();

        assertThatThrownBy(() -> service.release(
                GROUP_ID,
                EVENT_ID,
                WISH_ID,
                OWNER_ID
        )).isInstanceOf(ForbiddenOperationException.class);

        verifyNoInteractions(reservationRepository);
    }

    private void prepareEventAndWish() {
        Event event = Event.builder()
                .id(EVENT_ID)
                .status(EventStatus.ACTIVE)
                .build();

        when(eventRepository.findByIdAndGroupId(EVENT_ID, GROUP_ID))
                .thenReturn(Optional.of(event));

        when(wishRepository.findById(WISH_ID))
                .thenReturn(Optional.of(createWish(event)));
    }

    private Wish createWish(Event event) {
        return Wish.builder()
                .id(WISH_ID)
                .user(User.builder().id(OWNER_ID).build())
                .event(event)
                .name("Headphones")
                .build();
    }

    private GiftReservation createReservation() {
        Event event = Event.builder()
                .id(EVENT_ID)
                .status(EventStatus.ACTIVE)
                .build();

        return GiftReservation.builder()
                .id(RESERVATION_ID)
                .user(User.builder().id(REQUESTER_ID).build())
                .wish(createWish(event))
                .reservedAt(LocalDateTime.of(2026, 10, 2, 12, 30))
                .build();
    }
}