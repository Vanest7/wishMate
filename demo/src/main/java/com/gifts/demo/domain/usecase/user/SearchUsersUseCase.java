package com.gifts.demo.domain.usecase.user;

import com.gifts.demo.domain.model.User;

import java.util.List;

public interface SearchUsersUseCase {

    List<User> execute(String query);
}
