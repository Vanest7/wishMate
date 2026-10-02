package com.gifts.demo.domain.service.impl;

import com.gifts.demo.common.exception.ForbiddenOperationException;
import com.gifts.demo.common.exception.ResourceNotFoundException;
import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.model.EventStatus;
import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.repository.EventRepository;
import com.gifts.demo.domain.repository.GroupRepository;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.service.GroupPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    private static final Integer GROUP_ID = 1;
    private static final Integer EVENT_ID = 4;
    private static final Integer REQUESTER_ID = 20;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupPermissionService groupPermissionService;

    private EventServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EventServiceImpl(
                eventRepository,
                groupRepository,
                userRepository,
                groupPermissionService,
                new EventStateServiceImpl()
        );
    }

    @Test
    void shouldCreateActiveEventWithAuthenticatedCreator() {
        prepareCreation();

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Event input = Event.builder()
                .id(99)
                .name("  Birthday  ")
                .group(Group.builder().id(99).build())
                .creator(User.builder().id(99).build())
                .createdAt(LocalDateTime.of(2020, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2020, 1, 1, 0, 0))
                .build();

        Event result = service.create(GROUP_ID, REQUESTER_ID, input);

        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("Birthday");
        assertThat(result.getGroup().getId()).isEqualTo(GROUP_ID);
        assertThat(result.getCreator().getId()).isEqualTo(REQUESTER_ID);
        assertThat(result.getStatus()).isEqualTo(EventStatus.ACTIVE);
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();

        verify(groupPermissionService)
                .requireMembership(GROUP_ID, REQUESTER_ID);
    }

    @Test
    void shouldAllowCreationAsDraft() {
        prepareCreation();

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Event input = Event.builder()
                .name("Birthday")
                .status(EventStatus.DRAFT)
                .build();

        Event result = service.create(GROUP_ID, REQUESTER_ID, input);

        assertThat(result.getStatus()).isEqualTo(EventStatus.DRAFT);
    }

    @ParameterizedTest
    @EnumSource(
            value = EventStatus.class,
            names = {"CLOSED", "ARCHIVED"}
    )
    void shouldRejectInvalidInitialState(EventStatus status) {
        prepareCreation();

        Event input = Event.builder()
                .name("Birthday")
                .status(status)
                .build();

        assertThatThrownBy(() ->
                service.create(GROUP_ID, REQUESTER_ID, input)
        ).isInstanceOf(ValidationException.class);

        verifyNoInteractions(eventRepository);
    }

    @Test
    void shouldRejectEndDateBeforeStartDate() {
        Event input = Event.builder()
                .name("Birthday")
                .startDate(LocalDate.of(2026, 10, 10))
                .endDate(LocalDate.of(2026, 10, 9))
                .build();

        assertThatThrownBy(() ->
                service.create(GROUP_ID, REQUESTER_ID, input)
        ).isInstanceOf(ValidationException.class);

        verifyNoInteractions(eventRepository, groupRepository);
    }

    @Test
    void shouldRejectBlankName() {
        Event input = Event.builder()
                .name(" ")
                .build();

        assertThatThrownBy(() ->
                service.create(GROUP_ID, REQUESTER_ID, input)
        ).isInstanceOf(ValidationException.class);

        verifyNoInteractions(eventRepository);
    }

    @Test
    void shouldFollowDraftActiveClosedArchivedLifecycle() {
        Event stored = createEvent(EventStatus.DRAFT);
        prepareExistingEvent(stored);

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.activate(
                GROUP_ID, EVENT_ID, REQUESTER_ID
        ).getStatus()).isEqualTo(EventStatus.ACTIVE);

        assertThat(service.close(
                GROUP_ID, EVENT_ID, REQUESTER_ID
        ).getStatus()).isEqualTo(EventStatus.CLOSED);

        assertThat(service.archive(
                GROUP_ID, EVENT_ID, REQUESTER_ID
        ).getStatus()).isEqualTo(EventStatus.ARCHIVED);

        verify(eventRepository, times(3)).save(stored);
    }

    @Test
    void shouldRejectReactivatingArchivedEvent() {
        prepareExistingEvent(createEvent(EventStatus.ARCHIVED));

        assertThatThrownBy(() ->
                service.activate(GROUP_ID, EVENT_ID, REQUESTER_ID)
        ).isInstanceOf(ValidationException.class);

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void shouldClearOptionalFieldsOnUpdateAndPreserveState() {
        Event stored = createEvent(EventStatus.ACTIVE);
        stored.setDescription("Original description");
        stored.setStartDate(LocalDate.of(2026, 10, 10));
        stored.setEndDate(LocalDate.of(2026, 10, 11));
        prepareExistingEvent(stored);

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Event input = Event.builder()
                .name("Updated birthday")
                .status(EventStatus.ARCHIVED)
                .build();

        Event result = service.update(
                GROUP_ID, EVENT_ID, REQUESTER_ID, input
        );

        assertThat(result.getId()).isEqualTo(EVENT_ID);
        assertThat(result.getName()).isEqualTo("Updated birthday");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getStartDate()).isNull();
        assertThat(result.getEndDate()).isNull();
        assertThat(result.getStatus()).isEqualTo(EventStatus.ACTIVE);
    }

    @Test
    void shouldRejectEditingClosedEvent() {
        prepareExistingEvent(createEvent(EventStatus.CLOSED));

        Event input = Event.builder()
                .name("Updated birthday")
                .build();

        assertThatThrownBy(() ->
                service.update(
                        GROUP_ID, EVENT_ID, REQUESTER_ID, input
                )
        ).isInstanceOf(ValidationException.class);

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void shouldRejectReadingEventOutsideRequestedGroup() {
        when(eventRepository.findByIdAndGroupId(EVENT_ID, GROUP_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getById(GROUP_ID, EVENT_ID, REQUESTER_ID)
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldRejectNonMemberBeforeReadingEvent() {
        doThrow(new ForbiddenOperationException("Not a group member"))
                .when(groupPermissionService)
                .requireMembership(GROUP_ID, REQUESTER_ID);

        assertThatThrownBy(() ->
                service.getById(GROUP_ID, EVENT_ID, REQUESTER_ID)
        ).isInstanceOf(ForbiddenOperationException.class);

        verifyNoInteractions(eventRepository);
    }

    private void prepareCreation() {
        when(groupRepository.findById(GROUP_ID))
                .thenReturn(Optional.of(
                        Group.builder().id(GROUP_ID).build()
                ));

        when(userRepository.findById(REQUESTER_ID))
                .thenReturn(Optional.of(
                        User.builder().id(REQUESTER_ID).build()
                ));
    }

    private void prepareExistingEvent(Event event) {
        when(eventRepository.findByIdAndGroupId(EVENT_ID, GROUP_ID))
                .thenReturn(Optional.of(event));
    }

    private Event createEvent(EventStatus status) {
        return Event.builder()
                .id(EVENT_ID)
                .name("Birthday")
                .status(status)
                .build();
    }
}