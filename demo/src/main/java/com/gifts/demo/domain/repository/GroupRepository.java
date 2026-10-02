package com.gifts.demo.domain.repository;

import com.gifts.demo.domain.model.Group;

import java.util.List;
import java.util.Optional;

public interface GroupRepository {

    Optional<Group> findById(Integer id);

    List<Group> findByCreatorId(Integer creatorId);

    Optional<Group> findByIdAndCreatorId(
            Integer groupId,
            Integer creatorId
    );

    boolean existsByIdAndCreatorId(
            Integer groupId,
            Integer creatorId
    );

    Group save(Group group);

    void delete(Integer id);
}