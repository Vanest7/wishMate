package com.gifts.demo.domain.usecase.group;

import com.gifts.demo.domain.model.Group;

public interface GetGroupUseCase {
    Group execute(Integer groupId, Integer currentUserId);
}
