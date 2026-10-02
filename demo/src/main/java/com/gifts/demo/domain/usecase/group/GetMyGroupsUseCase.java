package com.gifts.demo.domain.usecase.group;

import com.gifts.demo.domain.model.Group;
import java.util.List;

public interface GetMyGroupsUseCase {
    List<Group> execute(Integer currentUserId);
}
