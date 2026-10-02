package com.gifts.demo.persistence.dao.db;

public interface GroupLockDaoDb {
    boolean lockById(Integer groupId);
}
