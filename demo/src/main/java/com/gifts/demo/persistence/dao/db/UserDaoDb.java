package com.gifts.demo.persistence.dao.db;

import com.gifts.demo.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDaoDb extends GenericDaoDb<User> {

    Optional<User> findByEmail(String email);

    List<User> search(String query);

    boolean existsByEmail(String email);

    boolean existsByHandle(String handle);
}