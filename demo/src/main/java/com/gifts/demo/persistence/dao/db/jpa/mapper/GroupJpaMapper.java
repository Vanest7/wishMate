package com.gifts.demo.persistence.dao.db.jpa.mapper;

import com.gifts.demo.domain.model.Group;
import com.gifts.demo.persistence.dao.db.jpa.entity.GroupEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {
        UserJpaMapper.class
})
public interface GroupJpaMapper {

    GroupJpaMapper INSTANCE =
            Mappers.getMapper(GroupJpaMapper.class);

    Group toGroup(GroupEntity groupEntity);

    GroupEntity toGroupEntity(Group group);
}