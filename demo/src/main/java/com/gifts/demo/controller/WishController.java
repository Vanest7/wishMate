package com.gifts.demo.controller;

import com.gifts.demo.controller.mapper.WishWebMapper;
import com.gifts.demo.controller.webmodel.wish.CreateWishRequest;
import com.gifts.demo.controller.webmodel.wish.UpdateWishRequest;
import com.gifts.demo.controller.webmodel.wish.WishResponse;
import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.domain.usecase.wish.CreateWishUseCase;
import com.gifts.demo.domain.usecase.wish.DeleteWishUseCase;
import com.gifts.demo.domain.usecase.wish.GetWishUseCase;
import com.gifts.demo.domain.usecase.wish.GetWishesByEventUseCase;
import com.gifts.demo.domain.usecase.wish.GetWishesByUserUseCase;
import com.gifts.demo.domain.usecase.wish.UpdateWishUseCase;
import com.gifts.demo.security.currentUser.CurrentUserProvider;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/events/{eventId}/wishes")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class WishController {

    private final GetWishUseCase getWishUseCase;
    private final GetWishesByUserUseCase getWishesByUserUseCase;
    private final GetWishesByEventUseCase getWishesByEventUseCase;
    private final CreateWishUseCase createWishUseCase;
    private final UpdateWishUseCase updateWishUseCase;
    private final DeleteWishUseCase deleteWishUseCase;

    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<List<WishResponse>> getWishesByEvent(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("eventId") Integer eventId
    ) {
        List<Wish> wishes = getWishesByEventUseCase.execute(
                groupId,
                eventId,
                currentUserProvider.getCurrentUserId()
        );

        return ResponseEntity.ok(
                WishWebMapper.toResponseList(wishes)
        );
    }

    @GetMapping("/{wishId}")
    public ResponseEntity<WishResponse> getWish(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("eventId") Integer eventId,
            @PathVariable("wishId") Integer wishId
    ) {
        Wish wish = getWishUseCase.execute(
                groupId,
                eventId,
                wishId,
                currentUserProvider.getCurrentUserId()
        );

        return ResponseEntity.ok(
                WishWebMapper.toResponse(wish)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WishResponse>> getWishesByUser(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("eventId") Integer eventId,
            @PathVariable("userId") Integer userId
    ) {
        List<Wish> wishes = getWishesByUserUseCase.execute(
                groupId,
                eventId,
                currentUserProvider.getCurrentUserId(),
                userId
        );

        return ResponseEntity.ok(
                WishWebMapper.toResponseList(wishes)
        );
    }

    @PostMapping
    public ResponseEntity<WishResponse> createWish(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("eventId") Integer eventId,
            @Valid @RequestBody CreateWishRequest request
    ) {
        Wish wish = createWishUseCase.execute(
                groupId,
                eventId,
                currentUserProvider.getCurrentUserId(),
                WishWebMapper.toWish(request)
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{wishId}")
                .buildAndExpand(wish.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(WishWebMapper.toResponse(wish));
    }

    @PutMapping("/{wishId}")
    public ResponseEntity<WishResponse> updateWish(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("eventId") Integer eventId,
            @PathVariable("wishId") Integer wishId,
            @Valid @RequestBody UpdateWishRequest request
    ) {
        Wish wish = updateWishUseCase.execute(
                groupId,
                eventId,
                wishId,
                currentUserProvider.getCurrentUserId(),
                WishWebMapper.toWish(request)
        );

        return ResponseEntity.ok(
                WishWebMapper.toResponse(wish)
        );
    }

    @DeleteMapping("/{wishId}")
    public ResponseEntity<Void> deleteWish(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("eventId") Integer eventId,
            @PathVariable("wishId") Integer wishId
    ) {
        deleteWishUseCase.execute(
                groupId,
                eventId,
                wishId,
                currentUserProvider.getCurrentUserId()
        );

        return ResponseEntity.noContent().build();
    }
}