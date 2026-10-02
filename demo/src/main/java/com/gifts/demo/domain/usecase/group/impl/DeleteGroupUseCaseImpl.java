package com.gifts.demo.domain.usecase.group.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.repository.GroupRepository;
import com.gifts.demo.domain.repository.UserGroupRepository;
import com.gifts.demo.domain.repository.GroupLockRepository;
import com.gifts.demo.domain.service.GroupAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import com.gifts.demo.domain.usecase.group.DeleteGroupUseCase;


@DomainUseCase
@RequiredArgsConstructor
public class DeleteGroupUseCaseImpl implements DeleteGroupUseCase {
    private final GroupAccessService groupAccessService;
    private final GroupLockRepository groupLockRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;

    @Override
    @Transactional
    public void execute(Integer groupId, Integer currentUserId) {
        groupAccessService.requirePositiveId(groupId);
        groupLockRepository.lockById(groupId);

        groupAccessService.requireOwner(groupId, currentUserId);
        List<GroupMember> groupMembers = userGroupRepository.findByGroupId(groupId);
        for (GroupMember member : groupMembers) {
            userGroupRepository.delete(groupId, member.getUser().getId());
        }
        groupRepository.delete(groupId);
    }
}
