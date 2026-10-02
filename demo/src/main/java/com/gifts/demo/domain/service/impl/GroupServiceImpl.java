package com.gifts.demo.domain.service.impl;

import com.gifts.demo.common.annotation.DomainService;
import com.gifts.demo.common.annotation.DomainTransactional;
import com.gifts.demo.common.exception.ResourceAlreadyExistsException;
import com.gifts.demo.common.exception.ResourceNotFoundException;
import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.model.GroupRole;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.repository.GroupRepository;
import com.gifts.demo.domain.repository.UserGroupRepository;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.service.GroupPermissionService;
import com.gifts.demo.domain.service.GroupService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@DomainService
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final GroupPermissionService groupPermissionService;

    @Override
    public Group getById(Integer groupId) {
        if (groupId == null) {
            throw new ValidationException(
                    "Group id cannot be null"
            );
        }

        return groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group with id " + groupId
                        )
                );
    }

    @Override
    @DomainTransactional
    public Group createGroup(
            String name,
            Integer creatorId
    ) {
        validateGroupName(name);

        User creator = getUser(creatorId);

        Group group = Group.builder()
                .name(name)
                .creator(creator)
                .build();

        Group savedGroup = groupRepository.save(group);

        GroupMember ownerMembership = GroupMember.builder()
                .group(savedGroup)
                .user(creator)
                .role(GroupRole.OWNER)
                .build();

        userGroupRepository.save(ownerMembership);

        return savedGroup;
    }

    @Override
    public List<Group> getGroupsByUserId(Integer userId) {
        getUser(userId);

        return userGroupRepository
                .findByUserId(userId)
                .stream()
                .map(GroupMember::getGroup)
                .toList();
    }

    @Override
    public List<GroupMember> getMembers(
            Integer groupId,
            Integer requesterId
    ) {
        getById(groupId);

        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        return userGroupRepository.findByGroupId(groupId);
    }

    @Override
    @DomainTransactional
    public GroupMember addMember(
            Integer groupId,
            Integer requesterId,
            Integer userId
    ) {
        Group group = getById(groupId);

        groupPermissionService.requireManager(
                groupId,
                requesterId
        );

        if (userGroupRepository.existsByGroupIdAndUserId(
                groupId,
                userId
        )) {
            throw new ResourceAlreadyExistsException(
                    "User " + userId
                            + " already belongs to group "
                            + groupId
            );
        }

        User user = getUser(userId);

        GroupMember groupMember = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupRole.MEMBER)
                .build();

        return userGroupRepository.save(groupMember);
    }

    @Override
    @DomainTransactional
    public GroupMember changeMemberRole(
            Integer groupId,
            Integer requesterId,
            Integer userId,
            GroupRole role
    ) {
        getById(groupId);

        if (role == null) {
            throw new ValidationException(
                    "Group role cannot be null"
            );
        }

        groupPermissionService.requireOwner(
                groupId,
                requesterId
        );

        GroupMember groupMember =
                getMembership(groupId, userId);

        if (groupMember.getRole() == GroupRole.OWNER) {
            throw new ValidationException(
                    "The group owner cannot be demoted"
            );
        }

        if (role == GroupRole.OWNER) {
            throw new ValidationException(
                    "Group ownership cannot be assigned "
                            + "through role modification"
            );
        }

        groupMember.setRole(role);

        return userGroupRepository.save(groupMember);
    }

    @Override
    @DomainTransactional
    public void removeMember(
            Integer groupId,
            Integer requesterId,
            Integer userId
    ) {
        getById(groupId);

        groupPermissionService.requireManager(
                groupId,
                requesterId
        );

        GroupMember groupMember =
                getMembership(groupId, userId);

        if (groupMember.getRole() == GroupRole.OWNER) {
            throw new ValidationException(
                    "The group owner cannot be removed"
            );
        }

        userGroupRepository.delete(
                groupId,
                userId
        );
    }

    private User getUser(Integer userId) {
        if (userId == null) {
            throw new ValidationException(
                    "User id cannot be null"
            );
        }

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with id " + userId
                        )
                );
    }

    private GroupMember getMembership(
            Integer groupId,
            Integer userId
    ) {
        return userGroupRepository
                .findByGroupIdAndUserId(
                        groupId,
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User " + userId
                                        + " is not a member of group "
                                        + groupId
                        )
                );
    }

    private void validateGroupName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException(
                    "Group name cannot be empty"
            );
        }
    }
}