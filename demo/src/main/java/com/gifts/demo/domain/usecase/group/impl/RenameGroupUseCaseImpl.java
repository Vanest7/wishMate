package com.gifts.demo.domain.usecase.group.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.repository.GroupLockRepository;
import com.gifts.demo.domain.repository.GroupRepository;
import com.gifts.demo.domain.service.GroupAccessService;
import com.gifts.demo.domain.usecase.group.RenameGroupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@DomainUseCase
@RequiredArgsConstructor
public class RenameGroupUseCaseImpl implements RenameGroupUseCase {

    private final GroupAccessService groupAccessService;
    private final GroupLockRepository groupLockRepository;
    private final GroupRepository groupRepository;

    @Override
    @Transactional
    public Group execute(
            Integer groupId,
            String name,
            Integer currentUserId
    ) {
        groupAccessService.requirePositiveId(groupId);
        groupLockRepository.lockById(groupId);

        groupAccessService.requireManager(groupId, currentUserId);

        String normalizedName = groupAccessService.normalizeName(name);

        Group group = groupAccessService.findGroup(groupId);
        group.setName(normalizedName);

        return groupRepository.save(group);
    }
}