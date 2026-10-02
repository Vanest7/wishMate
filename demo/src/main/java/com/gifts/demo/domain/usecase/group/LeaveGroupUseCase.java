package com.gifts.demo.domain.usecase.group;


public interface LeaveGroupUseCase {
    void execute(Integer groupId, Integer currentUserId);
}
