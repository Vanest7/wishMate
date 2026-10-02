package com.gifts.demo.persistence.dao.db.jpa.repository;

import com.gifts.demo.persistence.dao.db.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository
        extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByMail(String mail);

    boolean existsByMail(String mail);
}