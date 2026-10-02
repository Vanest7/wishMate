package com.gifts.demo.persistence.repository.impl;

import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.model.GroupRole;
import com.gifts.demo.domain.repository.UserGroupRepository;
import com.gifts.demo.persistence.dao.db.UserGroupDaoDb;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserGroupRepositoryImpl implements UserGroupRepository {

    private final UserGroupDaoDb userGroupDaoDb;

    @Override
    public List<GroupMember> findByGroupId(Integer groupId) {
        return userGroupDaoDb.findByGroupId(groupId);
    }

    @Override
    public List<GroupMember> findByUserId(Integer userId) {
        return userGroupDaoDb.findByUserId(userId);
    }

    @Override
    public Optional<GroupMember> findByGroupIdAndUserId(
            Integer groupId,
            Integer userId
    ) {
        return userGroupDaoDb.findByGroupIdAndUserId(
                groupId,
                userId
        );
    }

    @Override
    public boolean existsByGroupIdAndUserId(
            Integer groupId,
            Integer userId
    ) {
        return userGroupDaoDb.existsByGroupIdAndUserId(
                groupId,
                userId
        );
    }

    @Override
    public boolean hasRole(
            Integer groupId,
            Integer userId,
            GroupRole role
    ) {
        return userGroupDaoDb.existsByGroupIdAndUserIdAndRole(
                groupId,
                userId,
                role
        );
    }

    @Override
    public boolean hasAnyRole(
            Integer groupId,
            Integer userId,
            Collection<GroupRole> roles
    ) {
        return userGroupDaoDb.existsByGroupIdAndUserIdAndRoleIn(
                groupId,
                userId,
                roles
        );
    }

    @Override
    public GroupMember save(GroupMember groupMember) {
        return userGroupDaoDb.save(groupMember);
    }

    @Override
    public void delete(
            Integer groupId,
            Integer userId
    ) {
        userGroupDaoDb.deleteByGroupIdAndUserId(
                groupId,
                userId
        );
    }
}