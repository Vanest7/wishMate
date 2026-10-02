package com.gifts.demo.controller.mapper;

import com.gifts.demo.controller.webmodel.group.GroupMemberResponse;
import com.gifts.demo.domain.model.GroupMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface GroupMemberWebMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.userName")
    GroupMemberResponse toGroupMemberResponse(GroupMember member);
}
