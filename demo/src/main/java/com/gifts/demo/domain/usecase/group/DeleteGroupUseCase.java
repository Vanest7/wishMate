package com.gifts.demo.domain.usecase.group;


public interface DeleteGroupUseCase {
    void execute(Integer groupId, Integer currentUserId);
}
