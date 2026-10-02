package com.gifts.demo.domain.usecase.group;

import com.gifts.demo.domain.model.Group;

public interface CreateGroupUseCase {

    Group execute(String name, Integer currentUserId);
}