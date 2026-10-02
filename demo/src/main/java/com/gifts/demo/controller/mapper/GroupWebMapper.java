package com.gifts.demo.controller.mapper;

import com.gifts.demo.controller.webmodel.group.GroupResponse;
import com.gifts.demo.domain.model.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GroupWebMapper {

    @Mapping(target = "creatorId", source = "creator.id")
    GroupResponse toGroupResponse(Group group);
}