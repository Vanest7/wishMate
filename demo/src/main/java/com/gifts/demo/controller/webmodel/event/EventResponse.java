package com.gifts.demo.controller.webmodel.event;

import com.gifts.demo.domain.model.EventStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EventResponse(
        Integer id,
        Integer groupId,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        EventStatus status,
        Integer creatorId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}