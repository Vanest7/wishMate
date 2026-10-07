package com.gifts.demo.persistence.dao.db.jpa.mapper;

import com.gifts.demo.domain.model.User;
import com.gifts.demo.persistence.dao.db.jpa.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserJpaMapper {

    UserJpaMapper INSTANCE = Mappers.getMapper(UserJpaMapper.class);

    @Mapping(source = "mail", target = "email")
    @Mapping(source = "handle", target = "handle")
    User toUser(UserEntity userEntity);

    @Mapping(source = "email", target = "mail")
    @Mapping(source = "handle", target = "handle")
    UserEntity toUserEntity(User user);
}