package com.gifts.demo.persistence.repository.impl;

import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.persistence.dao.db.UserDaoDb;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserDaoDb userDaoDb;

    @Override
    public Optional<User> findById(Integer id) {
        return userDaoDb.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userDaoDb.findByEmail(email);
    }

    @Override
    public List<User> search(String query) {
        return userDaoDb.search(query);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userDaoDb.existsByEmail(email);
    }

    @Override
    public boolean existsByHandle(String handle) {
        return userDaoDb.existsByHandle(handle);
    }

    @Override
    public User save(User user) {
        return userDaoDb.save(user);
    }

    @Override
    public void delete(Integer id) {
        userDaoDb.delete(id);
    }
}