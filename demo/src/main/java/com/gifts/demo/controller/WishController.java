package com.gifts.demo.controller;

import com.gifts.demo.controller.exception.ApiExceptionHandler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


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
import org.springframework.http.MediaType;
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

@Tag(name = "Wishes")
@RestController
@RequestMapping(
        value = "/api/groups/{groupId}/events/{eventId}/wishes",
        produces = MediaType.APPLICATION_JSON_VALUE
)
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

    @Operation(summary = "List event wishes",
            description = "Requires group membership. Returns an array of wishes.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Wishes returned",
                    useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Authentication is missing or the Bearer token is invalid or expired. Rejected by Spring Security; the application ErrorResponse body is not guaranteed.",
                    content = @Content),
        @ApiResponse(responseCode = "403",
                    description = "Access is denied or the CSRF token/cookie is missing or invalid. Application permission failures return ErrorResponse; Spring Security failures may have a different body.",
                    content = @Content),
        @ApiResponse(responseCode = "404",
                    description = "A resource required by this operation was not found. Permission checks may produce HTTP 403 first.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Get a wish",
            description = "The wish must belong to the specified event and the event to the specified group.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Wish returned",
                    useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Authentication is missing or the Bearer token is invalid or expired. Rejected by Spring Security; the application ErrorResponse body is not guaranteed.",
                    content = @Content),
        @ApiResponse(responseCode = "403",
                    description = "Access is denied or the CSRF token/cookie is missing or invalid. Application permission failures return ErrorResponse; Spring Security failures may have a different body.",
                    content = @Content),
        @ApiResponse(responseCode = "404",
                    description = "A resource required by this operation was not found. Permission checks may produce HTTP 403 first.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "List a member's event wishes",
            description = "Both the requester and the target user must belong to the group.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Wishes returned",
                    useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Authentication is missing or the Bearer token is invalid or expired. Rejected by Spring Security; the application ErrorResponse body is not guaranteed.",
                    content = @Content),
        @ApiResponse(responseCode = "403",
                    description = "Access is denied or the CSRF token/cookie is missing or invalid. Application permission failures return ErrorResponse; Spring Security failures may have a different body.",
                    content = @Content),
        @ApiResponse(responseCode = "404",
                    description = "A resource required by this operation was not found. Permission checks may produce HTTP 403 first.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Create a wish",
            description = "Creates a wish for the authenticated user. The event must be ACTIVE.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Wish created",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Location", description = "Created resource URI.",
                            schema = @Schema(type = "string", format = "uri"))),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Authentication is missing or the Bearer token is invalid or expired. Rejected by Spring Security; the application ErrorResponse body is not guaranteed.",
                    content = @Content),
        @ApiResponse(responseCode = "403",
                    description = "Access is denied or the CSRF token/cookie is missing or invalid. Application permission failures return ErrorResponse; Spring Security failures may have a different body.",
                    content = @Content),
        @ApiResponse(responseCode = "404",
                    description = "A resource required by this operation was not found. Permission checks may produce HTTP 403 first.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409",
                    description = "The operation conflicts with stored data or a domain constraint. See code and message for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Update a wish",
            description = "Only the wish owner can update it, and the event must be ACTIVE.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Wish updated",
                    useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Authentication is missing or the Bearer token is invalid or expired. Rejected by Spring Security; the application ErrorResponse body is not guaranteed.",
                    content = @Content),
        @ApiResponse(responseCode = "403",
                    description = "Access is denied or the CSRF token/cookie is missing or invalid. Application permission failures return ErrorResponse; Spring Security failures may have a different body.",
                    content = @Content),
        @ApiResponse(responseCode = "404",
                    description = "A resource required by this operation was not found. Permission checks may produce HTTP 403 first.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409",
                    description = "The operation conflicts with stored data or a domain constraint. See code and message for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Delete a wish",
            description = "Only the wish owner can delete it, and the event must be ACTIVE.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Wish deleted",
                    content = @Content),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Authentication is missing or the Bearer token is invalid or expired. Rejected by Spring Security; the application ErrorResponse body is not guaranteed.",
                    content = @Content),
        @ApiResponse(responseCode = "403",
                    description = "Access is denied or the CSRF token/cookie is missing or invalid. Application permission failures return ErrorResponse; Spring Security failures may have a different body.",
                    content = @Content),
        @ApiResponse(responseCode = "404",
                    description = "A resource required by this operation was not found. Permission checks may produce HTTP 403 first.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409",
                    description = "The operation conflicts with stored data or a domain constraint. See code and message for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
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