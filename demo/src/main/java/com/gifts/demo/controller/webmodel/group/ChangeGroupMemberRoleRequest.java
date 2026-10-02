package com.gifts.demo.controller.webmodel.group;

import com.gifts.demo.domain.model.GroupRole;
import jakarta.validation.constraints.NotNull;

public record ChangeGroupMemberRoleRequest(
        @NotNull(message = "Role is required") GroupRole role
) {}
