package com.gifts.demo.controller;

import com.gifts.demo.common.exception.ForbiddenOperationException;
import com.gifts.demo.common.exception.ResourceNotFoundException;
import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.domain.usecase.wish.CreateWishUseCase;
import com.gifts.demo.domain.usecase.wish.DeleteWishUseCase;
import com.gifts.demo.domain.usecase.wish.GetWishUseCase;
import com.gifts.demo.domain.usecase.wish.GetWishesByEventUseCase;
import com.gifts.demo.domain.usecase.wish.GetWishesByUserUseCase;
import com.gifts.demo.domain.usecase.wish.UpdateWishUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gifts.demo.controller.support.ControllerTest;

@WebMvcTest(WishController.class)
@ControllerTest
class WishControllerTest {

    private static final String BASE_URL =
            "/api/groups/1/events/4/wishes";

    private static final String VALID_BODY = """
            {
                "name": "Headphones",
                "description": "Wireless headphones",
                "purchaseUrl": "https://example.test/headphones",
                "estimatedPrice": 59.90
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetWishUseCase getWishUseCase;

    @MockitoBean
    private GetWishesByUserUseCase getWishesByUserUseCase;

    @MockitoBean
    private GetWishesByEventUseCase getWishesByEventUseCase;

    @MockitoBean
    private CreateWishUseCase createWishUseCase;

    @MockitoBean
    private UpdateWishUseCase updateWishUseCase;

    @MockitoBean
    private DeleteWishUseCase deleteWishUseCase;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldCreateWishAndReturnLocation() throws Exception {
        when(createWishUseCase.execute(
                eq(1), eq(4), eq(20), any(Wish.class)
        )).thenReturn(createWish());

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().jwt(token -> token.subject("20")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost" + BASE_URL + "/6"
                ))
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.name").value("Headphones"))
                .andExpect(jsonPath("$.estimatedPrice").value(59.90));

        verify(createWishUseCase).execute(
                eq(1),
                eq(4),
                eq(20),
                argThat(wish ->
                        "Headphones".equals(wish.getName())
                                && wish.getUser() == null
                                && wish.getEvent() == null
                                && wish.getId() == null
                )
        );
    }

    @Test
    void shouldListWishesForRequestedEvent() throws Exception {
        when(getWishesByEventUseCase.execute(1, 4, 20))
                .thenReturn(List.of(createWish()));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt().jwt(token -> token.subject("20"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(6));

        verify(getWishesByEventUseCase).execute(1, 4, 20);
    }

    @Test
    void shouldGetWishWithoutExposingReservationOrPassword()
            throws Exception {

        when(getWishUseCase.execute(1, 4, 6, 20))
                .thenReturn(createWish());

        mockMvc.perform(get(BASE_URL + "/6")
                        .with(jwt().jwt(token -> token.subject("20"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.reservation").doesNotExist())
                .andExpect(jsonPath("$.reservedAt").doesNotExist())
                .andExpect(jsonPath("$.reservedBy").doesNotExist())
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void shouldPassRequesterAndTargetUserInCorrectOrder()
            throws Exception {

        when(getWishesByUserUseCase.execute(1, 4, 20, 10))
                .thenReturn(List.of(createWish()));

        mockMvc.perform(get(BASE_URL + "/user/10")
                        .with(jwt().jwt(token -> token.subject("20"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(6));

        verify(getWishesByUserUseCase).execute(1, 4, 20, 10);
    }

    @Test
    void shouldUpdateWish() throws Exception {
        when(updateWishUseCase.execute(
                eq(1), eq(4), eq(6), eq(20), any(Wish.class)
        )).thenReturn(createWish());

        mockMvc.perform(put(BASE_URL + "/6")
                        .with(jwt().jwt(token -> token.subject("20")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6));

        verify(updateWishUseCase).execute(
                eq(1), eq(4), eq(6), eq(20), any(Wish.class)
        );
    }

    @Test
    void shouldDeleteWishAndReturnNoContent() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/6")
                        .with(jwt().jwt(token -> token.subject("20")))
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(deleteWishUseCase).execute(1, 4, 6, 20);
    }

    @Test
    void shouldRejectBlankNameBeforeCallingUseCase() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .with(jwt().jwt(token -> token.subject("20")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": " ",
                                    "description": "Wireless headphones"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());

        verifyNoInteractions(createWishUseCase);
    }

    @Test
    void shouldRejectNegativePriceBeforeCallingUseCase()
            throws Exception {

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().jwt(token -> token.subject("20")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Headphones",
                                    "description": "Wireless headphones",
                                    "estimatedPrice": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.estimatedPrice")
                        .exists());

        verifyNoInteractions(createWishUseCase);
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .with(jwt().jwt(token -> token.subject("20")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        verifyNoInteractions(createWishUseCase);
    }

    @Test
    void shouldReturnNotFoundForMissingWish() throws Exception {
        when(getWishUseCase.execute(1, 4, 99, 20))
                .thenThrow(new ResourceNotFoundException(
                        "Wish not found"
                ));

        mockMvc.perform(get(BASE_URL + "/99")
                        .with(jwt().jwt(token -> token.subject("20"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturnForbiddenWhenDeletionIsDenied() throws Exception {
        doThrow(new ForbiddenOperationException("Not the wish owner"))
                .when(deleteWishUseCase)
                .execute(1, 4, 6, 20);

        mockMvc.perform(delete(BASE_URL + "/6")
                        .with(jwt().jwt(token -> token.subject("20")))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void shouldRejectUnauthenticatedRead() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(getWishesByEventUseCase);
    }

    private Wish createWish() {
        return Wish.builder()
                .id(6)
                .user(User.builder()
                        .id(20)
                        .passwordHash("test-password-hash")
                        .build())
                .event(Event.builder().id(4).build())
                .name("Headphones")
                .description("Wireless headphones")
                .purchaseUrl("https://example.test/headphones")
                .estimatedPrice(new BigDecimal("59.90"))
                .build();
    }
}