package com.gifts.demo.controller.webmodel.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateEventRequest(
        @NotBlank(message = "Event name is required")
        @Size(
                max = 100,
                message = "Event name must not exceed 100 characters"
        )
        String name,

        @Size(
                max = 255,
                message = "Event description must not exceed 255 characters"
        )
        String description,

        LocalDate startDate,

        LocalDate endDate
) {
}