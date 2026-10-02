package com.gifts.demo.controller.mapper;

import com.gifts.demo.controller.webmodel.event.CreateEventRequest;
import com.gifts.demo.controller.webmodel.event.EventResponse;
import com.gifts.demo.controller.webmodel.event.UpdateEventRequest;
import com.gifts.demo.domain.model.Event;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface EventWebMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "status", source = "status")
    Event toEvent(CreateEventRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    Event toEvent(UpdateEventRequest request);

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "creatorId", source = "creator.id")
    EventResponse toEventResponse(Event event);
}