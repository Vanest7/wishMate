package com.gifts.demo.controller.mapper;

import com.gifts.demo.controller.webmodel.user.UserResponse;
import com.gifts.demo.controller.webmodel.user.SearchUserResponse;
import com.gifts.demo.domain.model.User;

public final class UserWebMapper {

    private UserWebMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getHandle(),
                user.getEmail()
        );
    }

    public static SearchUserResponse toSearchResponse(User user) {
        return new SearchUserResponse(
                user.getId(),
                user.getUserName(),
                user.getHandle()
        );
    }
}