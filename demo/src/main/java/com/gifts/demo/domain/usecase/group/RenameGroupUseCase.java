package com.gifts.demo.domain.usecase.group;

import com.gifts.demo.domain.model.Group;

public interface RenameGroupUseCase {
    Group execute(Integer groupId, String name, Integer currentUserId);
}
