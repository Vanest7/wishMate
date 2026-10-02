package com.gifts.demo.persistence.dao.db.jpa.mapper;

import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.persistence.dao.db.jpa.entity.UserGroupEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {
        GroupJpaMapper.class,
        UserJpaMapper.class
})
public interface UserGroupJpaMapper {

    UserGroupJpaMapper INSTANCE =
            Mappers.getMapper(UserGroupJpaMapper.class);

    GroupMember toGroupMember(UserGroupEntity userGroupEntity);

    @Mapping(target = "id", ignore = true)
    UserGroupEntity toUserGroupEntity(GroupMember groupMember);
}