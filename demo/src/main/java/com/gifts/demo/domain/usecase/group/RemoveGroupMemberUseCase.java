package com.gifts.demo.domain.usecase.group;


public interface RemoveGroupMemberUseCase {
    void execute(Integer groupId, Integer userId, Integer currentUserId);
}
