package com.gifts.demo.persistence.dao.db.jpa.repository;

import com.gifts.demo.domain.model.GroupRole;
import com.gifts.demo.persistence.dao.db.jpa.entity.UserGroupEntity;
import com.gifts.demo.persistence.dao.db.jpa.entity.UserGroupId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserGroupJpaRepository
        extends JpaRepository<UserGroupEntity, UserGroupId> {

    List<UserGroupEntity> findAllByGroup_Id(Integer groupId);

    List<UserGroupEntity> findAllByUser_Id(Integer userId);

    Optional<UserGroupEntity> findByGroup_IdAndUser_Id(
            Integer groupId,
            Integer userId
    );

    boolean existsByGroup_IdAndUser_Id(
            Integer groupId,
            Integer userId
    );

    boolean existsByGroup_IdAndUser_IdAndRole(
            Integer groupId,
            Integer userId,
            GroupRole role
    );

    boolean existsByGroup_IdAndUser_IdAndRoleIn(
            Integer groupId,
            Integer userId,
            Collection<GroupRole> roles
    );

    void deleteByGroup_IdAndUser_Id(
            Integer groupId,
            Integer userId
    );
}