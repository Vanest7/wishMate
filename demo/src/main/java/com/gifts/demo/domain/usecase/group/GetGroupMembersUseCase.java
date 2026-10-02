package com.gifts.demo.domain.usecase.group;

import com.gifts.demo.domain.model.GroupMember;
import java.util.List;

public interface GetGroupMembersUseCase {
    List<GroupMember> execute(Integer groupId, Integer currentUserId);
}
