package com.gifts.demo.domain.usecase.group;

import com.gifts.demo.domain.model.GroupMember;

public interface AddGroupMemberUseCase {
    GroupMember execute(Integer groupId, Integer userId, Integer currentUserId);
}
