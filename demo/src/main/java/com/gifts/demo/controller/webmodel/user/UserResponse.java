package com.gifts.demo.controller.webmodel.user;

public record UserResponse(
        Integer id,
        String userName,
        String email
) {
}