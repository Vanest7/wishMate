package com.gifts.demo.domain.service;

import com.gifts.demo.domain.model.User;

import java.util.Optional;

public interface UserService {

    User getById(Integer userId);

    Optional<User> findByEmail(String email);

    User create(User user);

    User update(User user);
}