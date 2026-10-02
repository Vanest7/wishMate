package com.gifts.demo.controller.webmodel.group;

import com.gifts.demo.domain.model.GroupRole;
import java.time.LocalDateTime;

public record GroupMemberResponse(
        Integer userId,
        String userName,
        GroupRole role,
        LocalDateTime joinedAt
) {}
