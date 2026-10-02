package com.gifts.demo.domain.service;

import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.model.GroupMember;

public interface GroupAccessService {

    GroupMember requireMember(Integer groupId, Integer currentUserId);

    GroupMember requireManager(Integer groupId, Integer currentUserId);

    Group requireOwner(Integer groupId, Integer currentUserId);

    Group findGroup(Integer groupId);

    GroupMember findMember(Integer groupId, Integer userId);

    boolean isOwner(Group group, GroupMember member);

    void requirePositiveId(Integer id);

    String normalizeName(String name);
}