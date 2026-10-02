package com.gifts.demo.persistence.dao.db.jpa;

import com.gifts.demo.domain.model.ListWithCount;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.persistence.dao.db.UserDaoDb;
import com.gifts.demo.persistence.dao.db.jpa.entity.UserEntity;
import com.gifts.demo.persistence.dao.db.jpa.mapper.UserJpaMapper;
import com.gifts.demo.persistence.dao.db.jpa.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Primary
@RequiredArgsConstructor
public class UserDaoJpa implements UserDaoDb {

    private final UserJpaRepository userJpaRepository;

    @Override
    public List<User> getAll() {
        return userJpaRepository
                .findAll()
                .stream()
                .map(UserJpaMapper.INSTANCE::toUser)
                .toList();
    }

    @Override
    public ListWithCount<User> getAll(int page, int size) {
        Page<UserEntity> userPage =
                userJpaRepository.findAll(PageRequest.of(page, size));

        return new ListWithCount<>(
                userPage.stream()
                        .map(UserJpaMapper.INSTANCE::toUser)
                        .toList(),
                userPage.getTotalElements()
        );
    }

    @Override
    public Optional<User> findById(Integer id) {
        return userJpaRepository
                .findById(id)
                .map(UserJpaMapper.INSTANCE::toUser);
    }

    @Override
    public Integer insert(User user) {
        UserEntity entity =
                UserJpaMapper.INSTANCE.toUserEntity(user);

        return userJpaRepository.save(entity).getId();
    }

    @Override
    public void update(User user) {
        userJpaRepository.save(
                UserJpaMapper.INSTANCE.toUserEntity(user)
        );
    }

    @Override
    public void delete(Integer id) {
        userJpaRepository.deleteById(id);
    }

    @Override
    public long count() {
        return userJpaRepository.count();
    }

    @Override
    public User save(User user) {
        UserEntity entity =
                UserJpaMapper.INSTANCE.toUserEntity(user);

        return UserJpaMapper.INSTANCE.toUser(
                userJpaRepository.save(entity)
        );
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository
                .findByMail(email)
                .map(UserJpaMapper.INSTANCE::toUser);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByMail(email);
    }
}