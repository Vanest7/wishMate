package com.gifts.demo.controller;

import com.gifts.demo.common.exception.ForbiddenOperationException;
import com.gifts.demo.common.exception.ResourceAlreadyExistsException;
import com.gifts.demo.common.exception.ResourceNotFoundException;
import com.gifts.demo.domain.model.GiftReservation;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.domain.usecase.reservation.CancelGiftReservationUseCase;
import com.gifts.demo.domain.usecase.reservation.CreateGiftReservationUseCase;
import com.gifts.demo.domain.usecase.reservation.GetGiftReservationUseCase;
import com.gifts.demo.domain.usecase.reservation.GetMyGiftReservationsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.gifts.demo.controller.support.ControllerTest;

@WebMvcTest(GiftReservationController.class)
@ControllerTest
class GiftReservationControllerTest {

    private static final String BASE_URL = "/api/reservations";

    private static final String RESERVATION_URL =
            BASE_URL + "/groups/1/events/4/wishes/6";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateGiftReservationUseCase createGiftReservationUseCase;

    @MockitoBean
    private GetMyGiftReservationsUseCase getMyGiftReservationsUseCase;

    @MockitoBean
    private GetGiftReservationUseCase getGiftReservationUseCase;

    @MockitoBean
    private CancelGiftReservationUseCase cancelGiftReservationUseCase;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldCreateReservationUsingAuthenticatedUser() throws Exception {
        when(createGiftReservationUseCase.execute(1, 4, 6, 20))
                .thenReturn(createReservation());

        mockMvc.perform(post(RESERVATION_URL)
                        .with(jwt().jwt(token -> token.subject("20")))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.wishId").value(6))
                .andExpect(jsonPath("$.reservedAt")
                        .value("2026-10-02T12:30:00"))
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(createGiftReservationUseCase).execute(1, 4, 6, 20);
    }

    @Test
    void shouldListReservationsForAuthenticatedUser() throws Exception {
        when(getMyGiftReservationsUseCase.execute(20))
                .thenReturn(List.of(createReservation()));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt().jwt(token -> token.subject("20"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].wishId").value(6))
                .andExpect(jsonPath("$[0].user").doesNotExist());

        verify(getMyGiftReservationsUseCase).execute(20);
    }

    @Test
    void shouldGetReservationByReservationId() throws Exception {
        when(getGiftReservationUseCase.execute(7, 20))
                .thenReturn(createReservation());

        mockMvc.perform(get(BASE_URL + "/7")
                        .with(jwt().jwt(token -> token.subject("20"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.wishId").value(6));

        verify(getGiftReservationUseCase).execute(7, 20);
    }

    @Test
    void shouldCancelReservationAndReturnNoContent() throws Exception {
        mockMvc.perform(delete(RESERVATION_URL)
                        .with(jwt().jwt(token -> token.subject("20")))
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(cancelGiftReservationUseCase).execute(1, 4, 6, 20);
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(getMyGiftReservationsUseCase);
    }

    @Test
    void shouldRejectCreationWithoutBearerAndCsrfToken() throws Exception {
        mockMvc.perform(post(RESERVATION_URL))
                .andExpect(status().isForbidden());

        verifyNoInteractions(createGiftReservationUseCase);
    }

    @Test
    void shouldCreateReservationWithBearerTokenWithoutCsrf() throws Exception {
        Jwt decodedJwt = Jwt.withTokenValue("test-access-token")
                .header("alg", "RS256")
                .subject("20")
                .build();

        when(jwtDecoder.decode("test-access-token"))
                .thenReturn(decodedJwt);

        when(createGiftReservationUseCase.execute(1, 4, 6, 20))
                .thenReturn(createReservation());

        mockMvc.perform(post(RESERVATION_URL)
                        .header("Authorization", "Bearer test-access-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.wishId").value(6));

        verify(jwtDecoder).decode("test-access-token");
        verify(createGiftReservationUseCase).execute(1, 4, 6, 20);
    }

    @Test
    void shouldReturnForbiddenWhenReservationAccessIsDenied()
            throws Exception {

        when(getGiftReservationUseCase.execute(7, 10))
                .thenThrow(new ForbiddenOperationException(
                        "Reservation access denied"
                ));

        mockMvc.perform(get(BASE_URL + "/7")
                        .with(jwt().jwt(token -> token.subject("10"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.reservedAt").doesNotExist())
                .andExpect(jsonPath("$.wishId").doesNotExist());
    }

    @Test
    void shouldReturnConflictForAlreadyReservedWish() throws Exception {
        when(createGiftReservationUseCase.execute(1, 4, 6, 20))
                .thenThrow(new ResourceAlreadyExistsException(
                        "Wish is already reserved"
                ));

        mockMvc.perform(post(RESERVATION_URL)
                        .with(jwt().jwt(token -> token.subject("20")))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_ALREADY_EXISTS"));
    }

    @Test
    void shouldReturnNotFoundForMissingReservation() throws Exception {
        when(getGiftReservationUseCase.execute(99, 20))
                .thenThrow(new ResourceNotFoundException(
                        "Reservation not found"
                ));

        mockMvc.perform(get(BASE_URL + "/99")
                        .with(jwt().jwt(token -> token.subject("20"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturnBadRequestForNonNumericReservationId()
            throws Exception {

        mockMvc.perform(get(BASE_URL + "/invalid")
                        .with(jwt().jwt(token -> token.subject("20"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        verifyNoInteractions(getGiftReservationUseCase);
    }

    private GiftReservation createReservation() {
        return GiftReservation.builder()
                .id(7)
                .user(User.builder().id(20).build())
                .wish(Wish.builder().id(6).build())
                .reservedAt(LocalDateTime.of(2026, 10, 2, 12, 30))
                .build();
    }
}