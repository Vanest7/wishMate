package com.gifts.demo.domain.usecase.auth;

import com.gifts.demo.common.exception.InvalidCredentialsException;
import com.gifts.demo.common.exception.ResourceAlreadyExistsException;
import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationUseCasesTest {

    private static final String EMAIL = "user@example.test";
    private static final String PASSWORD = "Test-password-123!";
    private static final String HASH = "test-generated-hash";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    private RegisterUserUseCase register;
    private AuthenticateUserUseCase authenticate;

    @BeforeEach
    void setUp() {
        register = new RegisterUserUseCase(
                userRepository,
                passwordService
        );

        authenticate = new AuthenticateUserUseCase(
                userRepository,
                passwordService
        );
    }

    @Test
    void shouldNormalizeRegistrationFieldsAndStoreOnlyPasswordHash() {
        User saved = createUser();

        when(passwordService.hash(PASSWORD)).thenReturn(HASH);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = register.execute(
                "  Test user  ",
                "  user@example.test  ",
                PASSWORD
        );

        assertThat(result).isSameAs(saved);

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User submitted = captor.getValue();

        assertThat(submitted.getId()).isNull();
        assertThat(submitted.getUserName()).isEqualTo("Test user");
        assertThat(submitted.getEmail()).isEqualTo(EMAIL);
        assertThat(submitted.getPasswordHash())
                .isEqualTo(HASH)
                .isNotEqualTo(PASSWORD);

        verify(userRepository).existsByEmail(EMAIL);
        verify(passwordService).hash(PASSWORD);
    }

    @Test
    void shouldRejectDuplicateEmailBeforeHashingPassword() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() ->
                register.execute("Test user", EMAIL, PASSWORD)
        ).isInstanceOf(ResourceAlreadyExistsException.class);

        verifyNoInteractions(passwordService);
        verify(userRepository, never()).save(any(User.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldRejectRegistrationWithoutUserName(String userName) {
        assertThatThrownBy(() ->
                register.execute(userName, EMAIL, PASSWORD)
        ).isInstanceOf(ValidationException.class);

        verifyNoInteractions(userRepository, passwordService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldRejectRegistrationWithoutEmail(String email) {
        assertThatThrownBy(() ->
                register.execute("Test user", email, PASSWORD)
        ).isInstanceOf(ValidationException.class);

        verifyNoInteractions(userRepository, passwordService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldRejectRegistrationWithoutPassword(String password) {
        assertThatThrownBy(() ->
                register.execute("Test user", EMAIL, password)
        ).isInstanceOf(ValidationException.class);

        verifyNoInteractions(userRepository, passwordService);
    }

    @Test
    void shouldAuthenticateWithMatchingPassword() {
        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(createUser()));

        when(passwordService.matches(PASSWORD, HASH))
                .thenReturn(true);

        Integer userId = authenticate.execute(
                "  user@example.test  ",
                PASSWORD
        );

        assertThat(userId).isEqualTo(20);

        verify(userRepository).findByEmail(EMAIL);
        verify(passwordService).matches(PASSWORD, HASH);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectUnknownEmail() {
        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authenticate.execute(EMAIL, PASSWORD)
        ).isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(passwordService);
    }

    @Test
    void shouldRejectIncorrectPassword() {
        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(createUser()));

        when(passwordService.matches("Wrong-password", HASH))
                .thenReturn(false);

        assertThatThrownBy(() ->
                authenticate.execute(EMAIL, "Wrong-password")
        ).isInstanceOf(InvalidCredentialsException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldRejectLoginWithoutEmail(String email) {
        assertThatThrownBy(() ->
                authenticate.execute(email, PASSWORD)
        ).isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(userRepository, passwordService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldRejectLoginWithoutPassword(String password) {
        assertThatThrownBy(() ->
                authenticate.execute(EMAIL, password)
        ).isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(userRepository, passwordService);
    }

    private User createUser() {
        return User.builder()
                .id(20)
                .userName("Test user")
                .email(EMAIL)
                .passwordHash(HASH)
                .build();
    }
}