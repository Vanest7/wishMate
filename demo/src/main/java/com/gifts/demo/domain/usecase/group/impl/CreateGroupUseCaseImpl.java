package com.gifts.demo.domain.usecase.group.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.common.exception.ResourceNotFoundException;
import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.model.GroupRole;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.repository.GroupRepository;
import com.gifts.demo.domain.repository.UserGroupRepository;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.usecase.group.CreateGroupUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

@DomainUseCase
@RequiredArgsConstructor
public class CreateGroupUseCaseImpl implements CreateGroupUseCase {

    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;
    private final Clock clock = Clock.systemUTC();

    @Override
    @Transactional
    public Group execute(String name, Integer currentUserId) {
        if (currentUserId == null) {
            throw new ValidationException(
                    "Current user ID is required"
            );
        }

        if (name == null || name.isBlank()) {
            throw new ValidationException(
                    "Group name is required"
            );
        }

        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Current user not found"
                ));

        Group group = Group.builder()
                .name(name.strip())
                .creator(creator)
                .build();

        Group savedGroup = groupRepository.save(group);

        GroupMember ownerMembership = GroupMember.builder()
                .group(savedGroup)
                .user(creator)
                .role(GroupRole.OWNER)
                .joinedAt(LocalDateTime.now(clock))
                .build();

        userGroupRepository.save(ownerMembership);

        return savedGroup;
    }
}