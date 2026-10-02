package com.gifts.demo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    private Integer id;
    private Group group;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private EventStatus status;
    private User creator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}