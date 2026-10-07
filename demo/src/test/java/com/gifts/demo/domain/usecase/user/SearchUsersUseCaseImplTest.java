package com.gifts.demo.domain.usecase.user;

import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.usecase.user.impl.SearchUsersUseCaseImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SearchUsersUseCaseImplTest {

    @Test
    void shouldRemoveOneLeadingAtBeforeSearching() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.search("ana.garcia")).thenReturn(List.of(
                User.builder().id(1).userName("Ana García").handle("ana.garcia").build()
        ));

        List<User> result = new SearchUsersUseCaseImpl(repository)
                .execute("@ana.garcia");

        assertThat(result).hasSize(1);
        verify(repository).search("ana.garcia");
    }

    @Test
    void shouldRejectQueriesShorterThanTwoCharacters() {
        UserRepository repository = mock(UserRepository.class);

        assertThatThrownBy(() -> new SearchUsersUseCaseImpl(repository).execute("_"))
                .isInstanceOf(ValidationException.class);

        verifyNoInteractions(repository);
    }
}
