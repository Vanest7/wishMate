package com.gifts.demo.domain.usecase.group.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.repository.UserGroupRepository;
import com.gifts.demo.domain.service.GroupAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import com.gifts.demo.domain.usecase.group.GetGroupMembersUseCase;


@DomainUseCase
@RequiredArgsConstructor
public class GetGroupMembersUseCaseImpl implements GetGroupMembersUseCase {
    private final GroupAccessService groupAccessService;
    private final UserGroupRepository userGroupRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GroupMember> execute(Integer groupId, Integer currentUserId) {

        groupAccessService.requireMember(groupId, currentUserId);
        return userGroupRepository.findByGroupId(groupId);
    }
}
