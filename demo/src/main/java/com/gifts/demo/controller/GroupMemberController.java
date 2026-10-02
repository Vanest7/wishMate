package com.gifts.demo.controller;

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

    @GetMapping
    public ResponseEntity<List<GroupMemberResponse>> getMembers(@PathVariable("groupId") Integer groupId) {
        return ResponseEntity.ok(getGroupMembersUseCase.execute(groupId, currentUserProvider.getCurrentUserId())
                .stream().map(groupMemberWebMapper::toGroupMemberResponse).toList());
    }

    @PostMapping
    public ResponseEntity<GroupMemberResponse> addMember(
            @PathVariable("groupId") Integer groupId,
            @Valid @RequestBody AddGroupMemberRequest request) {
        GroupMember member = addGroupMemberUseCase.execute(
                groupId, request.userId(), currentUserProvider.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupMemberWebMapper.toGroupMemberResponse(member));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<GroupMemberResponse> changeRole(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("userId") Integer userId,
            @Valid @RequestBody ChangeGroupMemberRoleRequest request) {
        GroupMember member = changeGroupMemberRoleUseCase.execute(
                groupId, userId, request.role(), currentUserProvider.getCurrentUserId());
        return ResponseEntity.ok(groupMemberWebMapper.toGroupMemberResponse(member));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("userId") Integer userId) {
        removeGroupMemberUseCase.execute(groupId, userId, currentUserProvider.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> leaveGroup(@PathVariable("groupId") Integer groupId) {
        leaveGroupUseCase.execute(groupId, currentUserProvider.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
