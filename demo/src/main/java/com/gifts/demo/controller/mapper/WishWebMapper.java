package com.gifts.demo.controller.mapper;

import com.gifts.demo.controller.webmodel.wish.CreateWishRequest;
import com.gifts.demo.controller.webmodel.wish.UpdateWishRequest;
import com.gifts.demo.controller.webmodel.wish.WishResponse;
import com.gifts.demo.domain.model.Wish;

import java.util.List;

public final class WishWebMapper {

    private WishWebMapper() {
    }

    public static Wish toWish(CreateWishRequest request) {
        return Wish.builder()
                .name(request.name())
                .description(request.description())
                .purchaseUrl(request.purchaseUrl())
                .estimatedPrice(request.estimatedPrice())
                .build();
    }

    public static Wish toWish(UpdateWishRequest request) {
        return Wish.builder()
                .name(request.name())
                .description(request.description())
                .purchaseUrl(request.purchaseUrl())
                .estimatedPrice(request.estimatedPrice())
                .build();
    }

    public static WishResponse toResponse(Wish wish) {
        return new WishResponse(
                wish.getId(),
                wish.getUser() != null
                        ? wish.getUser().getId()
                        : null,
                wish.getEvent() != null
                        ? wish.getEvent().getId()
                        : null,
                wish.getName(),
                wish.getDescription(),
                wish.getPurchaseUrl(),
                wish.getEstimatedPrice()
        );
    }

    public static List<WishResponse> toResponseList(
            List<Wish> wishes
    ) {
        return wishes.stream()
                .map(WishWebMapper::toResponse)
                .toList();
    }
}