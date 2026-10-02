package com.gifts.demo.persistence.dao.db.jpa;

import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.model.ListWithCount;
import com.gifts.demo.persistence.dao.db.GroupDaoDb;
import com.gifts.demo.persistence.dao.db.jpa.entity.GroupEntity;
import com.gifts.demo.persistence.dao.db.jpa.mapper.GroupJpaMapper;
import com.gifts.demo.persistence.dao.db.jpa.repository.GroupJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Primary
@RequiredArgsConstructor
public class GroupDaoJpa implements GroupDaoDb {

    private final GroupJpaRepository groupJpaRepository;

    @Override
    public List<Group> getAll() {
        return groupJpaRepository
                .findAll()
                .stream()
                .map(GroupJpaMapper.INSTANCE::toGroup)
                .toList();
    }

    @Override
    public ListWithCount<Group> getAll(int page, int size) {
        Page<GroupEntity> groupPage =
                groupJpaRepository.findAll(PageRequest.of(page, size));

        return new ListWithCount<>(
                groupPage.stream()
                        .map(GroupJpaMapper.INSTANCE::toGroup)
                        .toList(),
                groupPage.getTotalElements()
        );
    }

    @Override
    public Optional<Group> findById(Integer id) {
        return groupJpaRepository
                .findById(id)
                .map(GroupJpaMapper.INSTANCE::toGroup);
    }

    @Override
    public Integer insert(Group group) {
        GroupEntity entity =
                GroupJpaMapper.INSTANCE.toGroupEntity(group);

        return groupJpaRepository.save(entity).getId();
    }

    @Override
    public void update(Group group) {
        groupJpaRepository.save(
                GroupJpaMapper.INSTANCE.toGroupEntity(group)
        );
    }

    @Override
    public void delete(Integer id) {
        groupJpaRepository.deleteById(id);
    }

    @Override
    public long count() {
        return groupJpaRepository.count();
    }

    @Override
    public Group save(Group group) {
        GroupEntity entity =
                GroupJpaMapper.INSTANCE.toGroupEntity(group);

        return GroupJpaMapper.INSTANCE.toGroup(
                groupJpaRepository.save(entity)
        );
    }

    @Override
    public List<Group> findByCreatorId(Integer creatorId) {
        return groupJpaRepository
                .findAllByCreator_Id(creatorId)
                .stream()
                .map(GroupJpaMapper.INSTANCE::toGroup)
                .toList();
    }

    @Override
    public Optional<Group> findByIdAndCreatorId(
            Integer groupId,
            Integer creatorId
    ) {
        return groupJpaRepository
                .findByIdAndCreator_Id(groupId, creatorId)
                .map(GroupJpaMapper.INSTANCE::toGroup);
    }

    @Override
    public boolean existsByIdAndCreatorId(
            Integer groupId,
            Integer creatorId
    ) {
        return groupJpaRepository
                .existsByIdAndCreator_Id(groupId, creatorId);
    }
}