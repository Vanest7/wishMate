package com.gifts.demo.controller;

import com.gifts.demo.controller.exception.ApiExceptionHandler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import com.gifts.demo.controller.mapper.GroupWebMapper;
import com.gifts.demo.controller.webmodel.group.CreateGroupRequest;
import com.gifts.demo.controller.webmodel.group.GroupResponse;
import com.gifts.demo.controller.webmodel.group.RenameGroupRequest;
import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.usecase.group.CreateGroupUseCase;
import com.gifts.demo.domain.usecase.group.DeleteGroupUseCase;
import com.gifts.demo.domain.usecase.group.GetGroupUseCase;
import com.gifts.demo.domain.usecase.group.GetMyGroupsUseCase;
import com.gifts.demo.domain.usecase.group.RenameGroupUseCase;
import com.gifts.demo.security.currentUser.CurrentUserProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Groups")
@RestController
@RequestMapping(
        value = "/api/groups",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class GroupController {

    private final CreateGroupUseCase createGroupUseCase;
    private final GetMyGroupsUseCase getMyGroupsUseCase;
    private final GetGroupUseCase getGroupUseCase;
    private final RenameGroupUseCase renameGroupUseCase;
    private final DeleteGroupUseCase deleteGroupUseCase;

    private final CurrentUserProvider currentUserProvider;
    private final GroupWebMapper groupWebMapper;

    @Operation(summary = "Create a group",
            description = "Creates the group and its owner membership in one transaction.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Group created",
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
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request
    ) {
        Group group = createGroupUseCase.execute(
                request.name(),
                currentUserProvider.getCurrentUserId()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{groupId}")
                .buildAndExpand(group.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(groupWebMapper.toGroupResponse(group));
    }

    @Operation(summary = "List my groups",
            description = "Returns the groups to which the authenticated user belongs. An empty result is an empty array.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Groups returned",
                    useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Authentication is missing or the Bearer token is invalid or expired. Rejected by Spring Security; the application ErrorResponse body is not guaranteed.",
                    content = @Content),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getMyGroups() {
        List<GroupResponse> groups = getMyGroupsUseCase
                .execute(currentUserProvider.getCurrentUserId())
                .stream()
                .map(groupWebMapper::toGroupResponse)
                .toList();

        return ResponseEntity.ok(groups);
    }

    @Operation(summary = "Get a group",
            description = "Requires group membership. Access checks may reject the request before a missing resource is reported.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Group returned",
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
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(
            @PathVariable("groupId") Integer groupId
    ) {
        Group group = getGroupUseCase.execute(
                groupId,
                currentUserProvider.getCurrentUserId()
        );

        return ResponseEntity.ok(
                groupWebMapper.toGroupResponse(group)
        );
    }

    @Operation(summary = "Rename a group",
            description = "Renames a group subject to the existing group permission checks.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Group renamed",
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
    @PatchMapping("/{groupId}")
    public ResponseEntity<GroupResponse> renameGroup(
            @PathVariable("groupId") Integer groupId,
            @Valid @RequestBody RenameGroupRequest request
    ) {
        Group group = renameGroupUseCase.execute(
                groupId,
                request.name(),
                currentUserProvider.getCurrentUserId()
        );

        return ResponseEntity.ok(
                groupWebMapper.toGroupResponse(group)
        );
    }

    @Operation(summary = "Delete a group",
            description = "Requires group ownership. Returns no response body.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Group deleted",
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
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable("groupId") Integer groupId
    ) {
        deleteGroupUseCase.execute(
                groupId,
                currentUserProvider.getCurrentUserId()
        );

        return ResponseEntity.noContent().build();
    }
}