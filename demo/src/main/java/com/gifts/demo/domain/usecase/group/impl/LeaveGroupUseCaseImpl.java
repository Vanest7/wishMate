package com.gifts.demo.domain.usecase.group.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.common.exception.GroupOperationException;
import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.repository.UserGroupRepository;
import com.gifts.demo.domain.service.GroupAccessService;
import com.gifts.demo.domain.repository.GroupLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import static com.gifts.demo.common.exception.GroupOperationException.Reason.*;
import com.gifts.demo.domain.usecase.group.LeaveGroupUseCase;


@DomainUseCase
@RequiredArgsConstructor
public class LeaveGroupUseCaseImpl implements LeaveGroupUseCase {
    private final GroupAccessService groupAccessService;
    private final GroupLockRepository groupLockRepository;
    private final UserGroupRepository userGroupRepository;

    @Override
    @Transactional
    public void execute(Integer groupId, Integer currentUserId) {
        groupAccessService.requirePositiveId(groupId);
        groupLockRepository.lockById(groupId);

        GroupMember member = groupAccessService.requireMember(groupId, currentUserId);
        Group group = groupAccessService.findGroup(groupId);
        if (groupAccessService.isOwner(group, member)) {
            throw new GroupOperationException(CONFLICT, "The owner cannot leave the group");
        }
        userGroupRepository.delete(groupId, currentUserId);
    }
}
