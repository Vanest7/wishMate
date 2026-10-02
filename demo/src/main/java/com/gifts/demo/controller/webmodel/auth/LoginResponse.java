package com.gifts.demo.controller.webmodel.auth;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}