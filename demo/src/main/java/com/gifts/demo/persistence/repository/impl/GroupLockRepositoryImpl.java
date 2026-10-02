package com.gifts.demo.persistence.repository.impl;

import com.gifts.demo.common.exception.GroupOperationException;
import com.gifts.demo.domain.repository.GroupLockRepository;
import com.gifts.demo.persistence.dao.db.GroupLockDaoDb;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupLockRepositoryImpl implements GroupLockRepository {
    private final GroupLockDaoDb groupLockDaoDb;

    @Override
    public void lockById(Integer groupId) {
        if (!groupLockDaoDb.lockById(groupId)) {
            throw new GroupOperationException(GroupOperationException.Reason.NOT_FOUND,
                    "Group not found or not accessible");
        }
    }
}
