package com.gifts.demo.domain.usecase.group.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.repository.UserGroupRepository;

import com.gifts.demo.domain.service.GroupAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import com.gifts.demo.domain.usecase.group.GetMyGroupsUseCase;


@DomainUseCase
@RequiredArgsConstructor
public class GetMyGroupsUseCaseImpl implements GetMyGroupsUseCase {
    private final UserGroupRepository userGroupRepository;
    private final GroupAccessService groupAccessService;

    @Override
    @Transactional(readOnly = true)
    public List<Group> execute(Integer currentUserId) {

        groupAccessService.requirePositiveId(currentUserId);
        return userGroupRepository.findByUserId(currentUserId).stream()
                .map(GroupMember::getGroup)
                .toList();
    }
}
