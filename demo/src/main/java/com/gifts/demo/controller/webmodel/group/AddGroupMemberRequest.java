package com.gifts.demo.controller.webmodel.group;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddGroupMemberRequest(
        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be positive")
        Integer userId
) {}
