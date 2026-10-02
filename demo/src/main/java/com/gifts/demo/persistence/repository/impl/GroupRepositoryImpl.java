package com.gifts.demo.persistence.repository.impl;

import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.repository.GroupRepository;
import com.gifts.demo.persistence.dao.db.GroupDaoDb;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GroupRepositoryImpl implements GroupRepository {

    private final GroupDaoDb groupDaoDb;

    @Override
    public Optional<Group> findById(Integer id) {
        return groupDaoDb.findById(id);
    }

    @Override
    public List<Group> findByCreatorId(Integer creatorId) {
        return groupDaoDb.findByCreatorId(creatorId);
    }

    @Override
    public Optional<Group> findByIdAndCreatorId(
            Integer groupId,
            Integer creatorId
    ) {
        return groupDaoDb.findByIdAndCreatorId(
                groupId,
                creatorId
        );
    }

    @Override
    public boolean existsByIdAndCreatorId(
            Integer groupId,
            Integer creatorId
    ) {
        return groupDaoDb.existsByIdAndCreatorId(
                groupId,
                creatorId
        );
    }

    @Override
    public Group save(Group group) {
        return groupDaoDb.save(group);
    }

    @Override
    public void delete(Integer id) {
        groupDaoDb.delete(id);
    }
}