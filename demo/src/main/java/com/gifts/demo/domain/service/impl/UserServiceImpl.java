package com.gifts.demo.domain.service.impl;

import com.gifts.demo.common.annotation.DomainService;
import com.gifts.demo.common.annotation.DomainTransactional;
import com.gifts.demo.common.exception.ResourceAlreadyExistsException;
import com.gifts.demo.common.exception.ResourceNotFoundException;
import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.service.UserService;
import com.gifts.demo.domain.service.HandlePolicy;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@DomainService
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getById(Integer userId) {
        if (userId == null) {
            throw new ValidationException("User id cannot be null");
        }

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with id " + userId
                        )
                );
    }

    @Override
    public Optional<User> findByEmail(String email) {
        validateEmail(email);

        return userRepository.findByEmail(email);
    }

    @Override
    @DomainTransactional
    public User create(User user) {
        validateUser(user);

        user.setHandle(HandlePolicy.normalize(user.getHandle()));
        if (userRepository.existsByHandle(user.getHandle())) {
            throw new ResourceAlreadyExistsException(
                    "User with handle " + user.getHandle()
            );
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "User with email " + user.getEmail()
            );
        }

        user.setId(null);

        return userRepository.save(user);
    }

    @Override
    @DomainTransactional
    public User update(User user) {
        validateUser(user);

        if (user.getId() == null) {
            throw new ValidationException(
                    "User id is required for update"
            );
        }

        User currentUser = getById(user.getId());

        if (user.getHandle() == null) {
            user.setHandle(currentUser.getHandle());
        } else {
            user.setHandle(HandlePolicy.normalize(user.getHandle()));
        }

        boolean handleChanged =
                currentUser.getHandle() == null
                        ? user.getHandle() != null
                        : !currentUser.getHandle().equals(user.getHandle());

        if (handleChanged && userRepository.existsByHandle(user.getHandle())) {
            throw new ResourceAlreadyExistsException(
                    "User with handle " + user.getHandle()
            );
        }

        boolean emailChanged =
                !currentUser.getEmail()
                        .equalsIgnoreCase(user.getEmail());

        if (emailChanged
                && userRepository.existsByEmail(user.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "User with email " + user.getEmail()
            );
        }

        return userRepository.save(user);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new ValidationException("User cannot be null");
        }

        validateEmail(user.getEmail());

        if (user.getUserName() == null
                || user.getUserName().isBlank()) {
            throw new ValidationException(
                    "User name cannot be empty"
            );
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException(
                    "Email cannot be empty"
            );
        }
    }
}