package com.gifts.demo.domain.usecase.auth;

import com.gifts.demo.common.annotation.DomainService;
import com.gifts.demo.common.exception.ResourceAlreadyExistsException;
import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.service.PasswordService;
import com.gifts.demo.domain.service.HandlePolicy;
import lombok.RequiredArgsConstructor;

@DomainService
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public User execute(
            String userName,
            String handle,
            String email,
            String password
    ) {
        validate(userName, email, password);

        String normalizedUserName = userName.trim();
        String normalizedHandle = HandlePolicy.normalize(handle);
        String normalizedEmail = email.trim();

        if (userRepository.existsByHandle(normalizedHandle)) {
            throw new ResourceAlreadyExistsException(
                    "A user with this handle already exists"
            );
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResourceAlreadyExistsException(
                    "A user with this email already exists"
            );
        }

        User user = User.builder()
                .userName(normalizedUserName)
                .handle(normalizedHandle)
                .email(normalizedEmail)
                .passwordHash(passwordService.hash(password))
                .build();

        return userRepository.save(user);
    }

    private void validate(
            String userName,
            String email,
            String password
    ) {
        if (userName == null || userName.isBlank()) {
            throw new ValidationException(
                    "User name is required"
            );
        }

        if (email == null || email.isBlank()) {
            throw new ValidationException(
                    "Email is required"
            );
        }

        if (password == null || password.isBlank()) {
            throw new ValidationException(
                    "Password is required"
            );
        }
    }
}