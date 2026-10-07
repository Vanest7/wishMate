package com.gifts.demo.controller.webmodel.user;

import io.swagger.v3.oas.annotations.media.Schema;

public record SearchUserResponse(
        Integer id,
        String userName,
        @Schema(
                description = "Public unique identifier; null for historical accounts not backfilled yet.",
                nullable = true
        )
        String handle
) {
}
