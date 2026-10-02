package com.gifts.demo.domain.repository;

import com.gifts.demo.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Integer id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);

    void delete(Integer id);
}