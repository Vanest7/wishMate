package com.gifts.demo.persistence.dao.db;

import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.model.GroupRole;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserGroupDaoDb {

    List<GroupMember> findByGroupId(Integer groupId);

    List<GroupMember> findByUserId(Integer userId);

    Optional<GroupMember> findByGroupIdAndUserId(
            Integer groupId,
            Integer userId
    );

    boolean existsByGroupIdAndUserId(
            Integer groupId,
            Integer userId
    );

    boolean existsByGroupIdAndUserIdAndRole(
            Integer groupId,
            Integer userId,
            GroupRole role
    );

    boolean existsByGroupIdAndUserIdAndRoleIn(
            Integer groupId,
            Integer userId,
            Collection<GroupRole> roles
    );

    GroupMember save(GroupMember groupMember);

    void deleteByGroupIdAndUserId(
            Integer groupId,
            Integer userId
    );
}