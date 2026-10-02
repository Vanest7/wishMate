package com.gifts.demo.domain.usecase.group.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.service.GroupAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import com.gifts.demo.domain.usecase.group.GetGroupUseCase;


@DomainUseCase
@RequiredArgsConstructor
public class GetGroupUseCaseImpl implements GetGroupUseCase {
    private final GroupAccessService groupAccessService;

    @Override
    @Transactional(readOnly = true)
    public Group execute(Integer groupId, Integer currentUserId) {

        groupAccessService.requireMember(groupId, currentUserId);
        return groupAccessService.findGroup(groupId);
    }
}
