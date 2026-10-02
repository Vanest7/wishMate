package com.gifts.demo.controller;

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

@RestController
@RequestMapping("/api/groups")
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

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getMyGroups() {
        List<GroupResponse> groups = getMyGroupsUseCase
                .execute(currentUserProvider.getCurrentUserId())
                .stream()
                .map(groupWebMapper::toGroupResponse)
                .toList();

        return ResponseEntity.ok(groups);
    }

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