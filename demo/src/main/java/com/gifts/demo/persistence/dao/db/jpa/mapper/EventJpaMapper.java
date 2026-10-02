package com.gifts.demo.persistence.dao.db.jpa.mapper;

import com.gifts.demo.domain.model.Event;
import com.gifts.demo.persistence.dao.db.jpa.entity.EventEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {
        GroupJpaMapper.class,
        UserJpaMapper.class
})
public interface EventJpaMapper {

    EventJpaMapper INSTANCE =
            Mappers.getMapper(EventJpaMapper.class);

    Event toEvent(EventEntity eventEntity);

    EventEntity toEventEntity(Event event);
}