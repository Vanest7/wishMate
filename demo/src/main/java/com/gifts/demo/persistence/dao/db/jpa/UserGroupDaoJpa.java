package com.gifts.demo.persistence.dao.db.jpa;

import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.model.GroupRole;
import com.gifts.demo.persistence.dao.db.UserGroupDaoDb;
import com.gifts.demo.persistence.dao.db.jpa.entity.UserGroupEntity;
import com.gifts.demo.persistence.dao.db.jpa.entity.UserGroupId;
import com.gifts.demo.persistence.dao.db.jpa.mapper.UserGroupJpaMapper;
import com.gifts.demo.persistence.dao.db.jpa.repository.UserGroupJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@Primary
@RequiredArgsConstructor
public class UserGroupDaoJpa implements UserGroupDaoDb {

    private final UserGroupJpaRepository userGroupJpaRepository;

    @Override
    public List<GroupMember> findByGroupId(Integer groupId) {
        return userGroupJpaRepository
                .findAllByGroup_Id(groupId)
                .stream()
                .map(UserGroupJpaMapper.INSTANCE::toGroupMember)
                .toList();
    }

    @Override
    public List<GroupMember> findByUserId(Integer userId) {
        return userGroupJpaRepository
                .findAllByUser_Id(userId)
                .stream()
                .map(UserGroupJpaMapper.INSTANCE::toGroupMember)
                .toList();
    }

    @Override
    public Optional<GroupMember> findByGroupIdAndUserId(
            Integer groupId,
            Integer userId
    ) {
        return userGroupJpaRepository
                .findByGroup_IdAndUser_Id(groupId, userId)
                .map(UserGroupJpaMapper.INSTANCE::toGroupMember);
    }

    @Override
    public boolean existsByGroupIdAndUserId(
            Integer groupId,
            Integer userId
    ) {
        return userGroupJpaRepository
                .existsByGroup_IdAndUser_Id(groupId, userId);
    }

    @Override
    public boolean existsByGroupIdAndUserIdAndRole(
            Integer groupId,
            Integer userId,
            GroupRole role
    ) {
        return userGroupJpaRepository
                .existsByGroup_IdAndUser_IdAndRole(
                        groupId,
                        userId,
                        role
                );
    }

    @Override
    public boolean existsByGroupIdAndUserIdAndRoleIn(
            Integer groupId,
            Integer userId,
            Collection<GroupRole> roles
    ) {
        return userGroupJpaRepository
                .existsByGroup_IdAndUser_IdAndRoleIn(
                        groupId,
                        userId,
                        roles
                );
    }

    @Override
    public GroupMember save(GroupMember groupMember) {
        UserGroupEntity entity =
                UserGroupJpaMapper.INSTANCE
                        .toUserGroupEntity(groupMember);

        UserGroupId id = new UserGroupId();
        id.setGroupId(groupMember.getGroup().getId());
        id.setUserId(groupMember.getUser().getId());

        entity.setId(id);

        UserGroupEntity savedEntity =
                userGroupJpaRepository.save(entity);

        return UserGroupJpaMapper.INSTANCE
                .toGroupMember(savedEntity);
    }

    @Override
    public void deleteByGroupIdAndUserId(
            Integer groupId,
            Integer userId
    ) {
        userGroupJpaRepository
                .deleteByGroup_IdAndUser_Id(groupId, userId);
    }
}