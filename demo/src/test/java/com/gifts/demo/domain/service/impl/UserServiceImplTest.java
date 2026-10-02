package com.gifts.demo.domain.service.impl;

import com.gifts.demo.common.exception.ResourceAlreadyExistsException;
import com.gifts.demo.common.exception.ResourceNotFoundException;
import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final Integer USER_ID = 20;

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository);
    }

    @Test
    void shouldReturnExistingUser() {
        User user = createUser();

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        assertThat(service.getById(USER_ID)).isSameAs(user);
    }

    @Test
    void shouldRejectMissingUser() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldRejectNullUserIdBeforeQueryingRepository() {
        assertThatThrownBy(() -> service.getById(null))
                .isInstanceOf(ValidationException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldClearSuppliedIdWhenCreatingUser() {
        User input = createUser();
        User saved = createUser();
        saved.setId(30);

        when(userRepository.save(any(User.class)))
                .thenReturn(saved);

        User result = service.create(input);

        assertThat(input.getId()).isNull();
        assertThat(result).isSameAs(saved);

        verify(userRepository).existsByEmail("user@example.test");
        verify(userRepository).save(input);
    }

    @Test
    void shouldRejectCreatingUserWithExistingEmail() {
        User input = createUser();

        when(userRepository.existsByEmail(input.getEmail()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(input))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectBlankUserName() {
        User input = createUser();
        input.setUserName(" ");

        assertThatThrownBy(() -> service.create(input))
                .isInstanceOf(ValidationException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectBlankEmail() {
        User input = createUser();
        input.setEmail(" ");

        assertThatThrownBy(() -> service.create(input))
                .isInstanceOf(ValidationException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectUpdateWithoutId() {
        User input = createUser();
        input.setId(null);

        assertThatThrownBy(() -> service.update(input))
                .isInstanceOf(ValidationException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldAllowUpdateWithoutChangingEmail() {
        User current = createUser();
        User input = createUser();
        input.setUserName("Updated name");

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(current));

        when(userRepository.save(input))
                .thenReturn(input);

        User result = service.update(input);

        assertThat(result.getUserName()).isEqualTo("Updated name");

        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository).save(input);
    }

    @Test
    void shouldTreatEmailCaseChangeAsSameEmail() {
        User input = createUser();
        input.setEmail("USER@example.test");

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(createUser()));

        when(userRepository.save(input))
                .thenReturn(input);

        assertThat(service.update(input)).isSameAs(input);

        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void shouldRejectChangingEmailToAnotherUsersEmail() {
        User input = createUser();
        input.setEmail("taken@example.test");

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(createUser()));

        when(userRepository.existsByEmail("taken@example.test"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.update(input))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldAllowChangingEmailToAvailableEmail() {
        User input = createUser();
        input.setEmail("available@example.test");

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(createUser()));

        when(userRepository.existsByEmail("available@example.test"))
                .thenReturn(false);

        when(userRepository.save(input))
                .thenReturn(input);

        User result = service.update(input);

        assertThat(result.getEmail())
                .isEqualTo("available@example.test");

        verify(userRepository).save(input);
    }

    private User createUser() {
        return User.builder()
                .id(USER_ID)
                .userName("Test user")
                .email("user@example.test")
                .passwordHash("test-password-hash")
                .build();
    }
}