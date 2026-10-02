package com.gifts.demo.persistence.dao.db.jpa.repository;

import com.gifts.demo.persistence.dao.db.jpa.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupJpaRepository extends JpaRepository<GroupEntity, Integer> {

    List<GroupEntity> findAllByCreator_Id(Integer creatorId);

    Optional<GroupEntity> findByIdAndCreator_Id(
            Integer groupId,
            Integer creatorId
    );

    boolean existsByIdAndCreator_Id(
            Integer groupId,
            Integer creatorId
    );
}