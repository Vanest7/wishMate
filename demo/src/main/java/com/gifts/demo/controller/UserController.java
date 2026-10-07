package com.gifts.demo.controller;

import com.gifts.demo.controller.exception.ApiExceptionHandler.ErrorResponse;
import com.gifts.demo.controller.mapper.UserWebMapper;
import com.gifts.demo.controller.webmodel.user.UserResponse;
import com.gifts.demo.controller.webmodel.user.SearchUserResponse;
import com.gifts.demo.domain.usecase.user.SearchUsersUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Users")
@RestController
@RequestMapping(
        value = "/api/users",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class UserController {

    private final SearchUsersUseCase searchUsersUseCase;

    @Operation(summary = "Search users",
            description = "Searches by visible name or public handle. A single leading @ is ignored. "
                    + "Searches use a minimum of 2 characters, return at most 20 users, and prioritize exact handles.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Users returned",
                useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400",
                description = "The search query is missing or blank.",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                description = "Authentication is missing or the token is invalid or expired.",
                content = @Content),
        @ApiResponse(responseCode = "500",
                description = "Unexpected application error.",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<SearchUserResponse>> search(
            @RequestParam(value = "query", required = false) String query
    ) {
        List<SearchUserResponse> users = searchUsersUseCase.execute(query)
                .stream()
                .map(UserWebMapper::toSearchResponse)
                .toList();

        return ResponseEntity.ok(users);
    }
}
