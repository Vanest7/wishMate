package com.gifts.demo.domain.service.impl;

import com.gifts.demo.common.exception.ForbiddenOperationException;
import com.gifts.demo.common.exception.ResourceNotFoundException;
import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.model.EventStatus;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.domain.repository.EventRepository;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.repository.WishRepository;
import com.gifts.demo.domain.service.GroupPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishServiceImplTest {

    private static final Integer GROUP_ID = 1;
    private static final Integer EVENT_ID = 4;
    private static final Integer WISH_ID = 6;
    private static final Integer OWNER_ID = 10;
    private static final Integer OTHER_USER_ID = 20;

    @Mock
    private WishRepository wishRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupPermissionService groupPermissionService;

    private WishServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WishServiceImpl(
                wishRepository,
                eventRepository,
                userRepository,
                groupPermissionService,
                new EventStateServiceImpl()
        );
    }

    @Test
    void shouldCreateWishWithAuthenticatedOwnerAndRequestedEvent() {
        prepareEvent();

        when(userRepository.findById(OTHER_USER_ID))
                .thenReturn(Optional.of(
                        User.builder().id(OTHER_USER_ID).build()
                ));

        when(wishRepository.save(any(Wish.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Wish input = createWish();
        input.setName("  Headphones  ");
        input.setDescription("  Wireless headphones  ");

        Wish result = service.create(
                GROUP_ID,
                EVENT_ID,
                OTHER_USER_ID,
                input
        );

        assertThat(result.getId()).isNull();
        assertThat(result.getUser().getId()).isEqualTo(OTHER_USER_ID);
        assertThat(result.getEvent().getId()).isEqualTo(EVENT_ID);
        assertThat(result.getName()).isEqualTo("Headphones");
        assertThat(result.getDescription()).isEqualTo("Wireless headphones");

        verify(groupPermissionService)
                .requireMembership(GROUP_ID, OTHER_USER_ID);
    }

    @Test
    void shouldRejectBlankName() {
        Wish input = createWish();
        input.setName(" ");

        assertThatThrownBy(() -> service.create(
                GROUP_ID, EVENT_ID, OWNER_ID, input
        )).isInstanceOf(ValidationException.class);

        verifyNoInteractions(wishRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "100000000.00", "1.001"})
    void shouldRejectInvalidPrice(String price) {
        Wish input = createWish();
        input.setEstimatedPrice(new BigDecimal(price));

        assertThatThrownBy(() -> service.create(
                GROUP_ID, EVENT_ID, OWNER_ID, input
        )).isInstanceOf(ValidationException.class);

        verifyNoInteractions(wishRepository);
    }

    @Test
    void shouldAllowOwnerToUpdateEditableFields() {
        prepareExistingWish();

        when(wishRepository.save(any(Wish.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Wish input = createWish();
        input.setName("Book");
        input.setPurchaseUrl(null);
        input.setUser(User.builder().id(OTHER_USER_ID).build());

        Wish result = service.update(
                GROUP_ID, EVENT_ID, WISH_ID, OWNER_ID, input
        );

        assertThat(result.getId()).isEqualTo(WISH_ID);
        assertThat(result.getUser().getId()).isEqualTo(OWNER_ID);
        assertThat(result.getEvent().getId()).isEqualTo(EVENT_ID);
        assertThat(result.getName()).isEqualTo("Book");
        assertThat(result.getPurchaseUrl()).isNull();
    }

    @Test
    void shouldRejectUpdatingAnotherUsersWish() {
        prepareExistingWish();

        assertThatThrownBy(() -> service.update(
                GROUP_ID, EVENT_ID, WISH_ID, OTHER_USER_ID, createWish()
        )).isInstanceOf(ForbiddenOperationException.class);

        verify(wishRepository, never()).save(any(Wish.class));
    }

    @Test
    void shouldAllowOwnerToDeleteWish() {
        prepareExistingWish();

        service.delete(GROUP_ID, EVENT_ID, WISH_ID, OWNER_ID);

        verify(wishRepository).delete(WISH_ID);
    }

    @Test
    void shouldRejectDeletingAnotherUsersWish() {
        prepareExistingWish();

        assertThatThrownBy(() -> service.delete(
                GROUP_ID, EVENT_ID, WISH_ID, OTHER_USER_ID
        )).isInstanceOf(ForbiddenOperationException.class);

        verify(wishRepository, never()).delete(any());
    }

    @Test
    void shouldRejectCreationInClosedEvent() {
        Event event = createEvent();
        event.setStatus(EventStatus.CLOSED);

        when(eventRepository.findByIdAndGroupId(EVENT_ID, GROUP_ID))
                .thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.create(
                GROUP_ID, EVENT_ID, OWNER_ID, createWish()
        )).isInstanceOf(ValidationException.class);

        verifyNoInteractions(wishRepository);
    }

    @Test
    void shouldRejectWishFromAnotherEvent() {
        prepareEvent();

        Wish wish = createWish();
        wish.setEvent(Event.builder().id(99).build());

        when(wishRepository.findById(WISH_ID))
                .thenReturn(Optional.of(wish));

        assertThatThrownBy(() -> service.getById(
                GROUP_ID, EVENT_ID, WISH_ID, OWNER_ID
        )).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldRejectNonMemberBeforeReadingWishes() {
        prepareEvent();

        doThrow(new ForbiddenOperationException("Not a group member"))
                .when(groupPermissionService)
                .requireMembership(GROUP_ID, OTHER_USER_ID);

        assertThatThrownBy(() -> service.getByEventId(
                GROUP_ID, EVENT_ID, OTHER_USER_ID
        )).isInstanceOf(ForbiddenOperationException.class);

        verifyNoInteractions(wishRepository);
    }

    private void prepareEvent() {
        when(eventRepository.findByIdAndGroupId(EVENT_ID, GROUP_ID))
                .thenReturn(Optional.of(createEvent()));
    }

    private void prepareExistingWish() {
        prepareEvent();

        when(wishRepository.findById(WISH_ID))
                .thenReturn(Optional.of(createWish()));
    }

    private Event createEvent() {
        return Event.builder()
                .id(EVENT_ID)
                .status(EventStatus.ACTIVE)
                .build();
    }

    private Wish createWish() {
        return Wish.builder()
                .id(WISH_ID)
                .user(User.builder().id(OWNER_ID).build())
                .event(createEvent())
                .name("Headphones")
                .description("Wireless headphones")
                .purchaseUrl("https://example.test/headphones")
                .estimatedPrice(new BigDecimal("59.90"))
                .build();
    }
}