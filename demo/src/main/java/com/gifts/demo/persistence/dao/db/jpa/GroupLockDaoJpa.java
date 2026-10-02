package com.gifts.demo.persistence.dao.db.jpa;

import com.gifts.demo.persistence.dao.db.GroupLockDaoDb;
import com.gifts.demo.persistence.dao.db.jpa.entity.GroupEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupLockDaoJpa implements GroupLockDaoDb {
    private final EntityManager entityManager;

    @Override
    public boolean lockById(Integer groupId) {
        return entityManager.find(GroupEntity.class, groupId, LockModeType.PESSIMISTIC_WRITE) != null;
    }
}
