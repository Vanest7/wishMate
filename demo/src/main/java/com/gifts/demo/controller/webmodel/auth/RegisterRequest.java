package com.gifts.demo.controller.webmodel.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterRequest(

        @NotBlank(message = "User name is required")
        String userName,

        @NotBlank(message = "Handle is required")
        @Schema(
                description = "Public unique identifier. After normalization, it contains 3 to 30 "
                        + "ASCII letters, numbers, dots or underscores. Outer spaces and uppercase letters are normalized.",
                example = "ana.garcia",
                minLength = 3,
                maxLength = 30,
                nullable = false
        )
        String handle,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        String password

) {

    @Override
    public String toString() {
        return "RegisterRequest[credentials=REDACTED]";
    }
}