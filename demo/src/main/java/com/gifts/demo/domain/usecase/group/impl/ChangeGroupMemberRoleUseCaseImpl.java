package com.gifts.demo.domain.usecase.group.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.common.exception.GroupOperationException;
import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.model.GroupRole;
import com.gifts.demo.domain.repository.UserGroupRepository;
import com.gifts.demo.domain.service.GroupAccessService;
import com.gifts.demo.domain.repository.GroupLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import static com.gifts.demo.common.exception.GroupOperationException.Reason.*;
import com.gifts.demo.domain.usecase.group.ChangeGroupMemberRoleUseCase;


@DomainUseCase
@RequiredArgsConstructor
public class ChangeGroupMemberRoleUseCaseImpl implements ChangeGroupMemberRoleUseCase {
    private final GroupAccessService groupAccessService;
    private final GroupLockRepository groupLockRepository;
    private final UserGroupRepository userGroupRepository;

    @Override
    @Transactional
    public GroupMember execute(Integer groupId, Integer userId, GroupRole role, Integer currentUserId) {
        groupAccessService.requirePositiveId(groupId);
        groupLockRepository.lockById(groupId);

        Group group = groupAccessService.requireOwner(groupId, currentUserId);
        if (role == null || role == GroupRole.OWNER) {
            throw new GroupOperationException(INVALID_INPUT, "Role must be ADMIN or MEMBER");
        }
        GroupMember member = groupAccessService.findMember(groupId, userId);
        if (groupAccessService.isOwner(group, member)) {
            throw new GroupOperationException(CONFLICT, "The owner role cannot be changed");
        }
        member.setRole(role);
        return userGroupRepository.save(member);
    }
}
