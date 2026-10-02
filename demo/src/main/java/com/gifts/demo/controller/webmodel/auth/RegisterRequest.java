package com.gifts.demo.controller.webmodel.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(

        @NotBlank(message = "User name is required")
        String userName,

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