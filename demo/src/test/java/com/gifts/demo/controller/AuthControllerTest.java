package com.gifts.demo.controller;

import com.gifts.demo.common.exception.InvalidCredentialsException;
import com.gifts.demo.common.exception.ResourceAlreadyExistsException;
import com.gifts.demo.controller.exception.ApiExceptionHandler;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.usecase.auth.AuthenticateUserUseCase;
import com.gifts.demo.domain.usecase.auth.RegisterUserUseCase;
import com.gifts.demo.security.JwtService;
import com.gifts.demo.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({
        SecurityConfig.class,
        ApiExceptionHandler.class
})
class AuthControllerTest {

    private static final String REGISTER_URL = "/api/auth/register";
    private static final String LOGIN_URL = "/api/auth/login";

    private static final String EMAIL = "user@example.test";
    private static final String HANDLE = "test.user";
    private static final String PASSWORD = "Test-password-123!";

    private static final String REGISTER_BODY = """
            {
                "userName": "Test user",
                "handle": "test.user",
                "email": "user@example.test",
                "password": "Test-password-123!"
            }
            """;

    private static final String LOGIN_BODY = """
            {
                "email": "user@example.test",
                "password": "Test-password-123!"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private AuthenticateUserUseCase authenticateUserUseCase;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void shouldProvideCsrfTokenWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Cache-Control",
                        containsString("no-store")
                ))
                .andExpect(jsonPath("$.headerName")
                        .value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(cookie().exists("XSRF-TOKEN"));

        verifyNoInteractions(
                registerUserUseCase,
                authenticateUserUseCase,
                jwtService
        );
    }

    @Test
    void shouldRegisterWithoutAuthenticationOrCsrf() throws Exception {
        User user = User.builder()
                .id(20)
                .userName("Test user")
                .handle(HANDLE)
                .email(EMAIL)
                .passwordHash("test-password-hash")
                .build();

        when(registerUserUseCase.execute("Test user", HANDLE, EMAIL, PASSWORD))
                .thenReturn(user);

        mockMvc.perform(post(REGISTER_URL)
                        .servletPath(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.userName").value("Test user"))
                .andExpect(jsonPath("$.handle").value(HANDLE))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(registerUserUseCase)
                .execute("Test user", HANDLE, EMAIL, PASSWORD);
    }

    @Test
    void shouldRejectInvalidRegistrationEmail() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .servletPath(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userName": "Test user",
                                    "handle": "test.user",
                                    "email": "invalid-email",
                                    "password": "Test-password-123!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        verifyNoInteractions(registerUserUseCase);
    }

    @Test
    void shouldRejectRegistrationWithoutHandle() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .servletPath(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userName": "Test user",
                                    "email": "user@example.test",
                                    "password": "Test-password-123!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.handle").exists());

        verifyNoInteractions(registerUserUseCase);
    }

    @Test
    void shouldReturnConflictForDuplicateRegistration() throws Exception {
        when(registerUserUseCase.execute("Test user", HANDLE, EMAIL, PASSWORD))
                .thenThrow(new ResourceAlreadyExistsException(
                        "A user with this email already exists"
                ));

        mockMvc.perform(post(REGISTER_URL)
                        .servletPath(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_ALREADY_EXISTS"));
    }

    @Test
    void shouldRejectLoginWithoutCsrf() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .servletPath(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY))
                .andExpect(status().isForbidden());

        verifyNoInteractions(authenticateUserUseCase, jwtService);
    }

    @Test
    void shouldLoginAndReturnAccessToken() throws Exception {
        Instant issuedAt = Instant.parse("2026-10-02T12:00:00Z");

        Jwt token = Jwt.withTokenValue("test-access-token")
                .header("alg", "RS256")
                .subject("20")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(900))
                .build();

        when(authenticateUserUseCase.execute(EMAIL, PASSWORD))
                .thenReturn(20);

        when(jwtService.createAccessToken(20))
                .thenReturn(token);

        mockMvc.perform(post(LOGIN_URL)
                        .servletPath(LOGIN_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Cache-Control",
                        containsString("no-store")
                ))
                .andExpect(jsonPath("$.accessToken")
                        .value("test-access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(authenticateUserUseCase).execute(EMAIL, PASSWORD);
        verify(jwtService).createAccessToken(20);
    }

    @Test
    void shouldRejectInvalidCredentialsWithoutIssuingToken()
            throws Exception {

        when(authenticateUserUseCase.execute(EMAIL, PASSWORD))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post(LOGIN_URL)
                        .servletPath(LOGIN_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_CREDENTIALS"));

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldRejectBlankLoginPasswordBeforeAuthentication()
            throws Exception {

        mockMvc.perform(post(LOGIN_URL)
                        .servletPath(LOGIN_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "user@example.test",
                                    "password": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        verifyNoInteractions(authenticateUserUseCase, jwtService);
    }
}