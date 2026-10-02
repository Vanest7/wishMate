package com.gifts.demo.controller.webmodel.wish;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateWishRequest(
        @NotBlank(message = "Wish name is required")
        @Size(
                max = 150,
                message = "Wish name must not exceed 150 characters"
        )
        String name,

        @Size(
                max = 500,
                message = "Wish description must not exceed 500 characters"
        )
        String description,

        @Size(
                max = 2048,
                message = "Purchase URL must not exceed 2048 characters"
        )
        String purchaseUrl,

        @DecimalMin(
                value = "0",
                message = "Estimated price cannot be negative"
        )
        @Digits(
                integer = 8,
                fraction = 2,
                message = "Estimated price must have at most 8 integer digits and 2 decimal places"
        )
        BigDecimal estimatedPrice
) {
}