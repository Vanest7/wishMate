package com.gifts.demo.domain.usecase.group.impl;

import com.gifts.demo.common.exception.GroupOperationException;
import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.model.GroupRole;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.repository.GroupLockRepository;
import com.gifts.demo.domain.repository.UserGroupRepository;
import com.gifts.demo.domain.service.GroupAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.gifts.demo.common.exception.GroupOperationException.Reason.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupMembershipUseCasesTest {

    private static final Integer GROUP_ID = 1;
    private static final Integer ACTOR_ID = 20;
    private static final Integer TARGET_ID = 30;

    @Mock
    private GroupAccessService groupAccessService;

    @Mock
    private GroupLockRepository groupLockRepository;

    @Mock
    private UserGroupRepository userGroupRepository;

    private LeaveGroupUseCaseImpl leaveGroup;
    private RemoveGroupMemberUseCaseImpl removeMember;

    @BeforeEach
    void setUp() {
        leaveGroup = new LeaveGroupUseCaseImpl(
                groupAccessService,
                groupLockRepository,
                userGroupRepository
        );

        removeMember = new RemoveGroupMemberUseCaseImpl(
                groupAccessService,
                groupLockRepository,
                userGroupRepository
        );
    }

    @Test
    void shouldAllowRegularMemberToLeaveGroup() {
        prepareLeaving(GroupRole.MEMBER, false);

        leaveGroup.execute(GROUP_ID, ACTOR_ID);

        verify(groupLockRepository).lockById(GROUP_ID);
        verify(userGroupRepository).delete(GROUP_ID, ACTOR_ID);
    }

    @Test
    void shouldPreventOwnerFromLeavingGroup() {
        prepareLeaving(GroupRole.OWNER, true);

        assertThatThrownBy(() ->
                leaveGroup.execute(GROUP_ID, ACTOR_ID)
        )
                .isInstanceOf(GroupOperationException.class)
                .hasMessage("The owner cannot leave the group")
                .extracting("reason")
                .isEqualTo(CONFLICT);

        verifyNoInteractions(userGroupRepository);
    }

    @Test
    void shouldAllowAdminToRemoveRegularMember() {
        prepareRemoval(GroupRole.ADMIN, GroupRole.MEMBER, false);

        removeMember.execute(GROUP_ID, TARGET_ID, ACTOR_ID);

        verify(groupLockRepository).lockById(GROUP_ID);
        verify(userGroupRepository).delete(GROUP_ID, TARGET_ID);
    }

    @Test
    void shouldPreventAdminFromRemovingAnotherAdmin() {
        prepareRemoval(GroupRole.ADMIN, GroupRole.ADMIN, false);

        assertThatThrownBy(() ->
                removeMember.execute(GROUP_ID, TARGET_ID, ACTOR_ID)
        )
                .isInstanceOf(GroupOperationException.class)
                .extracting("reason")
                .isEqualTo(FORBIDDEN);

        verify(userGroupRepository, never()).delete(anyInt(), anyInt());
    }

    @Test
    void shouldAllowOwnerToRemoveAdmin() {
        prepareRemoval(GroupRole.OWNER, GroupRole.ADMIN, false);

        removeMember.execute(GROUP_ID, TARGET_ID, ACTOR_ID);

        verify(userGroupRepository).delete(GROUP_ID, TARGET_ID);
    }

    @Test
    void shouldPreventRemovingGroupOwner() {
        prepareRemoval(GroupRole.ADMIN, GroupRole.OWNER, true);

        assertThatThrownBy(() ->
                removeMember.execute(GROUP_ID, TARGET_ID, ACTOR_ID)
        )
                .isInstanceOf(GroupOperationException.class)
                .extracting("reason")
                .isEqualTo(CONFLICT);

        verifyNoInteractions(userGroupRepository);
    }

    @Test
    void shouldRequireLeaveEndpointForSelfRemoval() {
        Group group = createGroup();
        GroupMember actor = createMember(ACTOR_ID, GroupRole.ADMIN);

        when(groupAccessService.requireManager(GROUP_ID, ACTOR_ID))
                .thenReturn(actor);
        when(groupAccessService.findGroup(GROUP_ID))
                .thenReturn(group);
        when(groupAccessService.findMember(GROUP_ID, ACTOR_ID))
                .thenReturn(actor);

        assertThatThrownBy(() ->
                removeMember.execute(GROUP_ID, ACTOR_ID, ACTOR_ID)
        )
                .isInstanceOf(GroupOperationException.class)
                .extracting("reason")
                .isEqualTo(INVALID_INPUT);

        verifyNoInteractions(userGroupRepository);
    }

    @Test
    void shouldStopRemovalWhenActorIsNotManager() {
        doThrow(new GroupOperationException(
                FORBIDDEN,
                "Management permission is required"
        )).when(groupAccessService)
                .requireManager(GROUP_ID, ACTOR_ID);

        assertThatThrownBy(() ->
                removeMember.execute(GROUP_ID, TARGET_ID, ACTOR_ID)
        )
                .isInstanceOf(GroupOperationException.class)
                .extracting("reason")
                .isEqualTo(FORBIDDEN);

        verify(groupAccessService, never())
                .findMember(GROUP_ID, TARGET_ID);
        verifyNoInteractions(userGroupRepository);
    }

    private void prepareLeaving(GroupRole role, boolean owner) {
        Group group = createGroup();
        GroupMember member = createMember(ACTOR_ID, role);

        when(groupAccessService.requireMember(GROUP_ID, ACTOR_ID))
                .thenReturn(member);
        when(groupAccessService.findGroup(GROUP_ID))
                .thenReturn(group);
        when(groupAccessService.isOwner(group, member))
                .thenReturn(owner);
    }

    private void prepareRemoval(
            GroupRole actorRole,
            GroupRole targetRole,
            boolean targetIsOwner
    ) {
        Group group = createGroup();
        GroupMember actor = createMember(ACTOR_ID, actorRole);
        GroupMember target = createMember(TARGET_ID, targetRole);

        when(groupAccessService.requireManager(GROUP_ID, ACTOR_ID))
                .thenReturn(actor);
        when(groupAccessService.findGroup(GROUP_ID))
                .thenReturn(group);
        when(groupAccessService.findMember(GROUP_ID, TARGET_ID))
                .thenReturn(target);
        when(groupAccessService.isOwner(group, target))
                .thenReturn(targetIsOwner);
    }

    private Group createGroup() {
        return Group.builder()
                .id(GROUP_ID)
                .name("Family")
                .build();
    }

    private GroupMember createMember(Integer userId, GroupRole role) {
        return GroupMember.builder()
                .group(createGroup())
                .user(User.builder().id(userId).build())
                .role(role)
                .build();
    }
}