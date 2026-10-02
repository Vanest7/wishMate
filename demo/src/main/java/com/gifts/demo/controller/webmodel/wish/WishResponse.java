package com.gifts.demo.controller.webmodel.wish;

import java.math.BigDecimal;

public record WishResponse(
        Integer id,
        Integer userId,
        Integer eventId,
        String description,
        String purchaseUrl,
        BigDecimal estimatedPrice
) {
}