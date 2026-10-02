package com.gifts.demo.domain.service.impl;

import com.gifts.demo.common.annotation.DomainService;
import com.gifts.demo.common.exception.GroupOperationException;
import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.model.GroupRole;
import com.gifts.demo.domain.repository.GroupRepository;
import com.gifts.demo.domain.repository.UserGroupRepository;
import com.gifts.demo.domain.service.GroupAccessService;
import com.gifts.demo.domain.service.GroupPermissionService;
import lombok.RequiredArgsConstructor;


import java.util.Objects;

import static com.gifts.demo.common.exception.GroupOperationException.Reason.INVALID_INPUT;
import static com.gifts.demo.common.exception.GroupOperationException.Reason.NOT_FOUND;

@DomainService
@RequiredArgsConstructor
public class GroupAccessServiceImpl implements GroupAccessService {

    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final GroupPermissionService groupPermissionService;

    @Override
    public GroupMember requireMember(
            Integer groupId,
            Integer currentUserId
    ) {
        groupPermissionService.requireMembership(groupId, currentUserId);

        return findMember(groupId, currentUserId);
    }

    @Override
    public GroupMember requireManager(
            Integer groupId,
            Integer currentUserId
    ) {
        groupPermissionService.requireManager(groupId, currentUserId);

        return findMember(groupId, currentUserId);
    }

    @Override
    public Group requireOwner(
            Integer groupId,
            Integer currentUserId
    ) {
        groupPermissionService.requireOwner(groupId, currentUserId);

        return findGroup(groupId);
    }

    @Override
    public Group findGroup(Integer groupId) {
        requirePositiveId(groupId);

        return groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupOperationException(
                        NOT_FOUND,
                        "Group not found"
                ));
    }

    @Override
    public GroupMember findMember(Integer groupId, Integer userId) {
        requirePositiveId(groupId);
        requirePositiveId(userId);

        return userGroupRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new GroupOperationException(
                        NOT_FOUND,
                        "Group member not found"
                ));
    }

    @Override
    public boolean isOwner(Group group, GroupMember member) {
        if (member.getRole() == GroupRole.OWNER) {
            return true;
        }

        return group.getCreator() != null
                && Objects.equals(
                group.getCreator().getId(),
                member.getUser().getId()
        );
    }

    @Override
    public void requirePositiveId(Integer id) {
        if (id == null || id <= 0) {
            throw new GroupOperationException(
                    INVALID_INPUT,
                    "A positive ID is required"
            );
        }
    }

    @Override
    public String normalizeName(String name) {
        if (name == null || name.isBlank() || name.length() > 100) {
            throw new GroupOperationException(
                    INVALID_INPUT,
                    "Group name must contain between 1 and 100 characters"
            );
        }

        return name.strip();
    }
}