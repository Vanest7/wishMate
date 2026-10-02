package com.gifts.demo.domain.repository;

import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.model.GroupRole;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserGroupRepository {

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

    boolean hasRole(
            Integer groupId,
            Integer userId,
            GroupRole role
    );

    boolean hasAnyRole(
            Integer groupId,
            Integer userId,
            Collection<GroupRole> roles
    );

    GroupMember save(GroupMember groupMember);

    void delete(
            Integer groupId,
            Integer userId
    );
}