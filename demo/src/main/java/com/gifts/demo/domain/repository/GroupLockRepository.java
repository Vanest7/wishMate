package com.gifts.demo.domain.repository;

public interface GroupLockRepository {
    void lockById(Integer groupId);
}
