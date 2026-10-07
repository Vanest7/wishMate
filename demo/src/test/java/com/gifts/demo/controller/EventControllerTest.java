package com.gifts.demo.controller;

import com.gifts.demo.common.exception.*;
import com.gifts.demo.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gifts.demo.controller.mapper.EventWebMapperImpl;
import com.gifts.demo.controller.support.ControllerTest;
import com.gifts.demo.domain.usecase.event.*;

@WebMvcTest(EventController.class)
@ControllerTest
@Import(EventWebMapperImpl.class)
class EventControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private CreateEventUseCase createEventUseCase;

    @MockitoBean
    private GetEventsByGroupUseCase getEventsByGroupUseCase;

    @MockitoBean
    private GetEventUseCase getEventUseCase;

    @MockitoBean
    private UpdateEventUseCase updateEventUseCase;

    @MockitoBean
    private ActivateEventUseCase activateEventUseCase;

    @MockitoBean
    private CloseEventUseCase closeEventUseCase;

    @MockitoBean
    private ArchiveEventUseCase archiveEventUseCase;

    private Event event(EventStatus status) {
        return Event.builder().id(40).group(Group.builder().id(10).build())
                .creator(User.builder().id(20).build()).name("Birthday")
                .description("Gift exchange").startDate(java.time.LocalDate.of(2027, 1, 1))
                .endDate(java.time.LocalDate.of(2027, 1, 2)).status(status).build();
    }

    @Test
    void shouldCreateEventAndMapRequest() throws Exception {
        when(createEventUseCase.execute(eq(10), eq(20), any(Event.class))).thenReturn(event(EventStatus.DRAFT));
        mockMvc.perform(post("/api/groups/10/events").with(jwt().jwt(t -> t.subject("20")))
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                                {"name":"Birthday","description":"Gift exchange",
                                 "startDate":"2027-01-01","endDate":"2027-01-02","status":"DRAFT"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/groups/10/events/40"))
                .andExpect(jsonPath("$.id").value(40)).andExpect(jsonPath("$.groupId").value(10))
                .andExpect(jsonPath("$.creatorId").value(20)).andExpect(jsonPath("$.status").value("DRAFT"));
        var captured = org.mockito.ArgumentCaptor.forClass(Event.class);
        verify(createEventUseCase).execute(eq(10), eq(20), captured.capture());
        org.assertj.core.api.Assertions.assertThat(captured.getValue())
                .extracting(Event::getName, Event::getDescription, Event::getStartDate, Event::getEndDate,
                        Event::getStatus, Event::getId, Event::getGroup, Event::getCreator)
                .containsExactly("Birthday", "Gift exchange", java.time.LocalDate.of(2027, 1, 1),
                        java.time.LocalDate.of(2027, 1, 2), EventStatus.DRAFT, null, null, null);
    }

    @Test
    void shouldListGroupEvents() throws Exception {
        when(getEventsByGroupUseCase.execute(10, 20)).thenReturn(List.of(event(EventStatus.ACTIVE)));
        mockMvc.perform(get("/api/groups/10/events").with(jwt().jwt(t -> t.subject("20"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(40)).andExpect(jsonPath("$[0].groupId").value(10));
        verify(getEventsByGroupUseCase).execute(10, 20);
    }

    @Test
    void shouldGetEvent() throws Exception {
        when(getEventUseCase.execute(10, 40, 20)).thenReturn(event(EventStatus.ACTIVE));
        mockMvc.perform(get("/api/groups/10/events/40").with(jwt().jwt(t -> t.subject("20"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Birthday"))
                .andExpect(jsonPath("$.startDate").value("2027-01-01"));
        verify(getEventUseCase).execute(10, 40, 20);
    }

    @Test
    void shouldUpdateEventAndMapRequest() throws Exception {
        Event updated = event(EventStatus.ACTIVE);
        updated.setName("New birthday");
        when(updateEventUseCase.execute(eq(10), eq(40), eq(20), any(Event.class))).thenReturn(updated);
        mockMvc.perform(put("/api/groups/10/events/40").with(jwt().jwt(t -> t.subject("20")))
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                                {"name":"New birthday","description":"Gift exchange",
                                 "startDate":"2027-01-01","endDate":"2027-01-02"}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("New birthday"));
        var captured = org.mockito.ArgumentCaptor.forClass(Event.class);
        verify(updateEventUseCase).execute(eq(10), eq(40), eq(20), captured.capture());
        org.assertj.core.api.Assertions.assertThat(captured.getValue())
                .extracting(Event::getName, Event::getDescription, Event::getStartDate, Event::getEndDate,
                        Event::getStatus, Event::getId, Event::getGroup, Event::getCreator)
                .containsExactly("New birthday", "Gift exchange", java.time.LocalDate.of(2027, 1, 1),
                        java.time.LocalDate.of(2027, 1, 2), null, null, null, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void shouldRejectBlankEventName(String method) throws Exception {
        String url = method.equals("POST") ? "/api/groups/10/events" : "/api/groups/10/events/40";
        mockMvc.perform(request(HttpMethod.valueOf(method), url)
                        .with(jwt().jwt(t -> t.subject("20"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.name").exists());
        verifyNoInteractions(createEventUseCase, updateEventUseCase);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"name\":\"Birthday\",\"startDate\":\"invalid\"}",
            "{\"name\":\"Birthday\",\"status\":\"UNKNOWN\"}"})
    void shouldRejectMalformedEventFields(String body) throws Exception {
        mockMvc.perform(post("/api/groups/10/events").with(jwt().jwt(t -> t.subject("20")))
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        verifyNoInteractions(createEventUseCase);
    }

    @Test
    void shouldReturnNotFound() throws Exception {
        when(getEventUseCase.execute(10, 40, 20)).thenThrow(new ResourceNotFoundException("Event not found"));
        mockMvc.perform(get("/api/groups/10/events/40").with(jwt().jwt(t -> t.subject("20"))))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturnForbiddenFromUseCase() throws Exception {
        when(getEventUseCase.execute(10, 40, 20)).thenThrow(new ForbiddenOperationException("Access denied"));
        mockMvc.perform(get("/api/groups/10/events/40").with(jwt().jwt(t -> t.subject("20"))))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void shouldReturnBadRequestForInvalidTransition() throws Exception {
        when(activateEventUseCase.execute(10, 40, 20)).thenThrow(new ValidationException("Invalid transition"));
        mockMvc.perform(post("/api/groups/10/events/40/activate")
                        .with(jwt().jwt(t -> t.subject("20"))).with(csrf()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldActivateEvent() throws Exception {
        when(activateEventUseCase.execute(10, 40, 20)).thenReturn(event(EventStatus.ACTIVE));
        mockMvc.perform(post("/api/groups/10/events/40/activate")
                        .with(jwt().jwt(t -> t.subject("20"))).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ACTIVE"));
        verify(activateEventUseCase).execute(10, 40, 20);
    }

    @Test
    void shouldCloseEvent() throws Exception {
        when(closeEventUseCase.execute(10, 40, 20)).thenReturn(event(EventStatus.CLOSED));
        mockMvc.perform(post("/api/groups/10/events/40/close")
                        .with(jwt().jwt(t -> t.subject("20"))).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CLOSED"));
        verify(closeEventUseCase).execute(10, 40, 20);
    }

    @Test
    void shouldArchiveEvent() throws Exception {
        when(archiveEventUseCase.execute(10, 40, 20)).thenReturn(event(EventStatus.ARCHIVED));
        mockMvc.perform(post("/api/groups/10/events/40/archive")
                        .with(jwt().jwt(t -> t.subject("20"))).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ARCHIVED"));
        verify(archiveEventUseCase).execute(10, 40, 20);
    }

    @Test
    void shouldRequireAuthenticationForListEvents() throws Exception {
        mockMvc.perform(request(HttpMethod.GET, "/api/groups/10/events").with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(createEventUseCase, getEventsByGroupUseCase, getEventUseCase, updateEventUseCase, activateEventUseCase, closeEventUseCase, archiveEventUseCase);
    }

    @Test
    void shouldRequireAuthenticationForGetEvent() throws Exception {
        mockMvc.perform(request(HttpMethod.GET, "/api/groups/10/events/40").with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(createEventUseCase, getEventsByGroupUseCase, getEventUseCase, updateEventUseCase, activateEventUseCase, closeEventUseCase, archiveEventUseCase);
    }

    @Test
    void shouldRequireAuthenticationForCreateEvent() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/groups/10/events").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Birthday\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(createEventUseCase, getEventsByGroupUseCase, getEventUseCase, updateEventUseCase, activateEventUseCase, closeEventUseCase, archiveEventUseCase);
    }

    @Test
    void shouldRequireCsrfForCreateEvent() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/groups/10/events").with(authenticatedUserWithoutCsrfBypass()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Birthday\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(createEventUseCase, getEventsByGroupUseCase, getEventUseCase, updateEventUseCase, activateEventUseCase, closeEventUseCase, archiveEventUseCase);
    }

    @Test
    void shouldRequireAuthenticationForUpdateEvent() throws Exception {
        mockMvc.perform(request(HttpMethod.PUT, "/api/groups/10/events/40").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Birthday\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(createEventUseCase, getEventsByGroupUseCase, getEventUseCase, updateEventUseCase, activateEventUseCase, closeEventUseCase, archiveEventUseCase);
    }

    @Test
    void shouldRequireCsrfForUpdateEvent() throws Exception {
        mockMvc.perform(request(HttpMethod.PUT, "/api/groups/10/events/40").with(authenticatedUserWithoutCsrfBypass()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Birthday\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(createEventUseCase, getEventsByGroupUseCase, getEventUseCase, updateEventUseCase, activateEventUseCase, closeEventUseCase, archiveEventUseCase);
    }

    @Test
    void shouldRequireAuthenticationForActivateEvent() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/groups/10/events/40/activate").with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(createEventUseCase, getEventsByGroupUseCase, getEventUseCase, updateEventUseCase, activateEventUseCase, closeEventUseCase, archiveEventUseCase);
    }

    @Test
    void shouldRequireCsrfForActivateEvent() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/groups/10/events/40/activate").with(authenticatedUserWithoutCsrfBypass()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(createEventUseCase, getEventsByGroupUseCase, getEventUseCase, updateEventUseCase, activateEventUseCase, closeEventUseCase, archiveEventUseCase);
    }

    @Test
    void shouldRequireAuthenticationForCloseEvent() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/groups/10/events/40/close").with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(createEventUseCase, getEventsByGroupUseCase, getEventUseCase, updateEventUseCase, activateEventUseCase, closeEventUseCase, archiveEventUseCase);
    }

    @Test
    void shouldRequireCsrfForCloseEvent() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/groups/10/events/40/close").with(authenticatedUserWithoutCsrfBypass()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(createEventUseCase, getEventsByGroupUseCase, getEventUseCase, updateEventUseCase, activateEventUseCase, closeEventUseCase, archiveEventUseCase);
    }

    @Test
    void shouldRequireAuthenticationForArchiveEvent() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/groups/10/events/40/archive").with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(createEventUseCase, getEventsByGroupUseCase, getEventUseCase, updateEventUseCase, activateEventUseCase, closeEventUseCase, archiveEventUseCase);
    }

    @Test
    void shouldRequireCsrfForArchiveEvent() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/groups/10/events/40/archive").with(authenticatedUserWithoutCsrfBypass()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(createEventUseCase, getEventsByGroupUseCase, getEventUseCase, updateEventUseCase, activateEventUseCase, closeEventUseCase, archiveEventUseCase);
    }

    private RequestPostProcessor authenticatedUserWithoutCsrfBypass() {
        Jwt token = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("20")
                .build();

        return authentication(new JwtAuthenticationToken(token));
    }
}
