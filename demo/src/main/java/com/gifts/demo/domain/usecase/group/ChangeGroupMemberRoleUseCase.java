package com.gifts.demo.domain.usecase.group;

import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.model.GroupRole;

public interface ChangeGroupMemberRoleUseCase {
    GroupMember execute(Integer groupId, Integer userId, GroupRole role, Integer currentUserId);
}
