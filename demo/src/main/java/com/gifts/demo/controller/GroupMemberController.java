package com.gifts.demo.controller;

import com.gifts.demo.controller.exception.ApiExceptionHandler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import com.gifts.demo.controller.mapper.GroupMemberWebMapper;
import com.gifts.demo.controller.webmodel.group.AddGroupMemberRequest;
import com.gifts.demo.controller.webmodel.group.ChangeGroupMemberRoleRequest;
import com.gifts.demo.controller.webmodel.group.GroupMemberResponse;
import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.usecase.group.AddGroupMemberUseCase;
import com.gifts.demo.domain.usecase.group.ChangeGroupMemberRoleUseCase;
import com.gifts.demo.domain.usecase.group.GetGroupMembersUseCase;
import com.gifts.demo.domain.usecase.group.LeaveGroupUseCase;
import com.gifts.demo.domain.usecase.group.RemoveGroupMemberUseCase;
import com.gifts.demo.security.currentUser.CurrentUserProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Group members")
@RestController
@RequestMapping("/api/groups/{groupId}/members")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class GroupMemberController {
    private final GetGroupMembersUseCase getGroupMembersUseCase;
    private final AddGroupMemberUseCase addGroupMemberUseCase;
    private final ChangeGroupMemberRoleUseCase changeGroupMemberRoleUseCase;
    private final RemoveGroupMemberUseCase removeGroupMemberUseCase;
    private final LeaveGroupUseCase leaveGroupUseCase;
    private final CurrentUserProvider currentUserProvider;
    private final GroupMemberWebMapper groupMemberWebMapper;

    @Operation(summary = "List group members",
            description = "Requires group membership.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Members returned",
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
    public ResponseEntity<List<GroupMemberResponse>> getMembers(@PathVariable("groupId") Integer groupId) {
        return ResponseEntity.ok(getGroupMembersUseCase.execute(groupId, currentUserProvider.getCurrentUserId())
                .stream().map(groupMemberWebMapper::toGroupMemberResponse).toList());
    }

    @Operation(summary = "Add a group member",
            description = "Requires group management permissions. An existing membership produces a conflict.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Member added",
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
    @PostMapping
    public ResponseEntity<GroupMemberResponse> addMember(
            @PathVariable("groupId") Integer groupId,
            @Valid @RequestBody AddGroupMemberRequest request) {
        GroupMember member = addGroupMemberUseCase.execute(
                groupId, request.userId(), currentUserProvider.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupMemberWebMapper.toGroupMemberResponse(member));
    }

    @Operation(summary = "Change a member role",
            description = "The requested role must be ADMIN or MEMBER. The owner role cannot be changed.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Role changed",
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
    @PatchMapping("/{userId}")
    public ResponseEntity<GroupMemberResponse> changeRole(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("userId") Integer userId,
            @Valid @RequestBody ChangeGroupMemberRoleRequest request) {
        GroupMember member = changeGroupMemberRoleUseCase.execute(
                groupId, userId, request.role(), currentUserProvider.getCurrentUserId());
        return ResponseEntity.ok(groupMemberWebMapper.toGroupMemberResponse(member));
    }

    @Operation(summary = "Remove a group member",
            description = "The owner cannot be removed. Use the leave endpoint to remove your own membership.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Member removed",
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
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("userId") Integer userId) {
        removeGroupMemberUseCase.execute(groupId, userId, currentUserProvider.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Leave a group",
            description = "The group owner cannot leave the group.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Membership removed",
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
    @DeleteMapping("/me")
    public ResponseEntity<Void> leaveGroup(@PathVariable("groupId") Integer groupId) {
        leaveGroupUseCase.execute(groupId, currentUserProvider.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
