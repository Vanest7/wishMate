package com.gifts.demo.domain.usecase.user.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.usecase.user.SearchUsersUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@DomainUseCase
@RequiredArgsConstructor
public class SearchUsersUseCaseImpl implements SearchUsersUseCase {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<User> execute(String query) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Search query cannot be empty");
        }

        String normalizedQuery = query.strip();
        if (normalizedQuery.startsWith("@")) {
            normalizedQuery = normalizedQuery.substring(1);
        }
        if (normalizedQuery.length() < 2) {
            throw new ValidationException("Search query must contain at least 2 characters");
        }

        return userRepository.search(normalizedQuery);
    }
}
