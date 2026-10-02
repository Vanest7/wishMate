package com.gifts.demo.persistence.dao.db;

import com.gifts.demo.domain.model.User;

import java.util.Optional;

public interface UserDaoDb extends GenericDaoDb<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}