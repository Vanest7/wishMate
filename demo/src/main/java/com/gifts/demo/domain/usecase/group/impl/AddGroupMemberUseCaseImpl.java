package com.gifts.demo.domain.usecase.group.impl;

import com.gifts.demo.common.annotation.DomainUseCase;
import com.gifts.demo.common.exception.GroupOperationException;
import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.model.GroupRole;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.repository.UserGroupRepository;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.repository.GroupLockRepository;
import com.gifts.demo.domain.service.GroupAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import static com.gifts.demo.common.exception.GroupOperationException.Reason.*;
import com.gifts.demo.domain.usecase.group.AddGroupMemberUseCase;


@DomainUseCase
@RequiredArgsConstructor
public class AddGroupMemberUseCaseImpl implements AddGroupMemberUseCase {
    private final GroupAccessService groupAccessService;
    private final GroupLockRepository groupLockRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public GroupMember execute(Integer groupId, Integer userId, Integer currentUserId) {
        groupAccessService.requirePositiveId(groupId);
        groupLockRepository.lockById(groupId);

        groupAccessService.requireManager(groupId, currentUserId);
        groupAccessService.requirePositiveId(userId);
        if (userGroupRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new GroupOperationException(CONFLICT, "User already belongs to the group");
        }
        Group group = groupAccessService.findGroup(groupId);
        if (Objects.equals(group.getCreator().getId(), userId)) {
            throw new GroupOperationException(CONFLICT, "The group owner membership must be repaired");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GroupOperationException(NOT_FOUND, "User not found"));
        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupRole.MEMBER)
                .joinedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        return userGroupRepository.save(member);
    }
}
