package com.gifts.demo.domain.service;

import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.model.GroupRole;

import java.util.List;

public interface GroupService {

    Group getById(Integer groupId);

    Group createGroup(
            String name,
            Integer creatorId
    );

    List<Group> getGroupsByUserId(Integer userId);

    List<GroupMember> getMembers(
            Integer groupId,
            Integer requesterId
    );

    GroupMember addMember(
            Integer groupId,
            Integer requesterId,
            Integer userId
    );

    GroupMember changeMemberRole(
            Integer groupId,
            Integer requesterId,
            Integer userId,
            GroupRole role
    );

    void removeMember(
            Integer groupId,
            Integer requesterId,
            Integer userId
    );
}