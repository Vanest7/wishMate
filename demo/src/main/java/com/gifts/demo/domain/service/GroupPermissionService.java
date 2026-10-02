package com.gifts.demo.domain.service;

public interface GroupPermissionService {

    void requireMembership(
            Integer groupId,
            Integer userId
    );

    void requireManager(
            Integer groupId,
            Integer userId
    );

    void requireOwner(
            Integer groupId,
            Integer userId
    );
}