package com.gifts.demo.domain.service.impl;

import com.gifts.demo.common.annotation.DomainService;
import com.gifts.demo.common.exception.ForbiddenOperationException;
import com.gifts.demo.domain.model.GroupRole;
import com.gifts.demo.domain.repository.UserGroupRepository;
import com.gifts.demo.domain.service.GroupPermissionService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@DomainService
@RequiredArgsConstructor
public class GroupPermissionServiceImpl
        implements GroupPermissionService {

    private final UserGroupRepository userGroupRepository;

    @Override
    public void requireMembership(
            Integer groupId,
            Integer userId
    ) {
        boolean isMember =
                userGroupRepository.existsByGroupIdAndUserId(
                        groupId,
                        userId
                );

        if (!isMember) {
            throw new ForbiddenOperationException(
                    "User " + userId
                            + " does not belong to group "
                            + groupId
            );
        }
    }

    @Override
    public void requireManager(
            Integer groupId,
            Integer userId
    ) {
        boolean canManage =
                userGroupRepository.hasAnyRole(
                        groupId,
                        userId,
                        List.of(
                                GroupRole.OWNER,
                                GroupRole.ADMIN
                        )
                );

        if (!canManage) {
            throw new ForbiddenOperationException(
                    "User " + userId
                            + " cannot manage group "
                            + groupId
            );
        }
    }

    @Override
    public void requireOwner(
            Integer groupId,
            Integer userId
    ) {
        boolean isOwner =
                userGroupRepository.hasRole(
                        groupId,
                        userId,
                        GroupRole.OWNER
                );

        if (!isOwner) {
            throw new ForbiddenOperationException(
                    "User " + userId
                            + " is not the owner of group "
                            + groupId
            );
        }
    }
}