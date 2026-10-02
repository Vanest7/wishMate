package com.gifts.demo.security.currentUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringSecurityCurrentUserProviderTest {

    private final SpringSecurityCurrentUserProvider provider =
            new SpringSecurityCurrentUserProvider();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnUserIdFromAuthenticatedJwtSubject() {
        authenticateWithSubject("20");

        assertThat(provider.getCurrentUserId()).isEqualTo(20);
    }

    @Test
    void shouldRejectMissingAuthentication() {
        assertThatThrownBy(provider::getCurrentUserId)
                .isInstanceOf(InsufficientAuthenticationException.class);
    }

    @Test
    void shouldRejectAuthenticationThatIsNotJwt() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "20",
                        "test-credentials",
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThatThrownBy(provider::getCurrentUserId)
                .isInstanceOf(InsufficientAuthenticationException.class);
    }

    @Test
    void shouldRejectUnauthenticatedJwt() {
        Jwt jwt = createJwt("20");

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(jwt, List.of());

        authentication.setAuthenticated(false);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThatThrownBy(provider::getCurrentUserId)
                .isInstanceOf(InsufficientAuthenticationException.class);
    }

    @Test
    void shouldRejectJwtWithoutSubject() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .claim("scope", "test")
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new JwtAuthenticationToken(jwt, List.of())
                );

        assertThatThrownBy(provider::getCurrentUserId)
                .isInstanceOf(BadCredentialsException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "not-a-number",
            "2147483648"
    })
    void shouldRejectInvalidSubject(String subject) {
        authenticateWithSubject(subject);

        assertThatThrownBy(provider::getCurrentUserId)
                .isInstanceOf(BadCredentialsException.class);
    }

    private void authenticateWithSubject(String subject) {
        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        createJwt(subject),
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private Jwt createJwt(String subject) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject(subject)
                .build();
    }
}