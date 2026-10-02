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
import java.util.Objects;
import static com.gifts.demo.common.exception.GroupOperationException.Reason.*;
import com.gifts.demo.domain.usecase.group.RemoveGroupMemberUseCase;


@DomainUseCase
@RequiredArgsConstructor
public class RemoveGroupMemberUseCaseImpl implements RemoveGroupMemberUseCase {
    private final GroupAccessService groupAccessService;
    private final GroupLockRepository groupLockRepository;
    private final UserGroupRepository userGroupRepository;

    @Override
    @Transactional
    public void execute(Integer groupId, Integer userId, Integer currentUserId) {
        groupAccessService.requirePositiveId(groupId);
        groupLockRepository.lockById(groupId);

        GroupMember actor = groupAccessService.requireManager(groupId, currentUserId);
        Group group = groupAccessService.findGroup(groupId);
        GroupMember target = groupAccessService.findMember(groupId, userId);
        if (groupAccessService.isOwner(group, target)) {
            throw new GroupOperationException(CONFLICT, "The group owner cannot be removed");
        }
        if (Objects.equals(userId, currentUserId)) {
            throw new GroupOperationException(INVALID_INPUT, "Use the leave group endpoint");
        }
        if (actor.getRole() == GroupRole.ADMIN && target.getRole() != GroupRole.MEMBER) {
            throw new GroupOperationException(FORBIDDEN, "Administrators can only remove regular members");
        }
        userGroupRepository.delete(groupId, userId);
    }
}
