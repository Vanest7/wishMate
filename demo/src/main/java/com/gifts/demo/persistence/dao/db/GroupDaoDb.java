package com.gifts.demo.persistence.dao.db;

import com.gifts.demo.domain.model.Group;

import java.util.List;
import java.util.Optional;

public interface GroupDaoDb extends GenericDaoDb<Group> {

    List<Group> findByCreatorId(Integer creatorId);

    Optional<Group> findByIdAndCreatorId(
            Integer groupId,
            Integer creatorId
    );

    boolean existsByIdAndCreatorId(
            Integer groupId,
            Integer creatorId
    );
}