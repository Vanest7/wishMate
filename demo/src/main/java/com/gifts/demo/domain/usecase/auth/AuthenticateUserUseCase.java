package com.gifts.demo.domain.usecase.auth;

import com.gifts.demo.common.annotation.DomainService;
import com.gifts.demo.common.exception.InvalidCredentialsException;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.service.PasswordService;
import lombok.RequiredArgsConstructor;

@DomainService
@RequiredArgsConstructor
public class AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public Integer execute(String email, String password) {
        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordService.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user.getId();
    }
}