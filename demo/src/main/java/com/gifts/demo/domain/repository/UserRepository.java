package com.gifts.demo.domain.repository;

import com.gifts.demo.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Integer id);

    Optional<User> findByEmail(String email);

    List<User> search(String query);

    boolean existsByEmail(String email);

    boolean existsByHandle(String handle);

    User save(User user);

    void delete(Integer id);
}