package com.gifts.demo.domain.service.impl;

import com.gifts.demo.common.exception.ResourceAlreadyExistsException;
import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.model.GroupRole;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.repository.GroupRepository;
import com.gifts.demo.domain.repository.UserGroupRepository;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.service.GroupPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    private static final Integer GROUP_ID = 1;
    private static final Integer OWNER_ID = 10;
    private static final Integer MEMBER_ID = 20;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private GroupPermissionService groupPermissionService;

    private GroupServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GroupServiceImpl(
                groupRepository,
                userRepository,
                userGroupRepository,
                groupPermissionService
        );
    }

    @Test
    void shouldCreateGroupAndRegisterCreatorAsOwner() {
        User creator = User.builder().id(OWNER_ID).build();
        Group savedGroup = createGroup();

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(creator));

        when(groupRepository.save(any(Group.class)))
                .thenReturn(savedGroup);

        Group result = service.createGroup("Family", OWNER_ID);

        assertThat(result).isSameAs(savedGroup);

        ArgumentCaptor<Group> groupCaptor =
                ArgumentCaptor.forClass(Group.class);

        verify(groupRepository).save(groupCaptor.capture());

        assertThat(groupCaptor.getValue().getName()).isEqualTo("Family");
        assertThat(groupCaptor.getValue().getCreator()).isSameAs(creator);
        assertThat(groupCaptor.getValue().getId()).isNull();

        ArgumentCaptor<GroupMember> memberCaptor =
                ArgumentCaptor.forClass(GroupMember.class);

        verify(userGroupRepository).save(memberCaptor.capture());

        GroupMember membership = memberCaptor.getValue();

        assertThat(membership.getGroup()).isSameAs(savedGroup);
        assertThat(membership.getUser()).isSameAs(creator);
        assertThat(membership.getRole()).isEqualTo(GroupRole.OWNER);
    }

    @Test
    void shouldRejectBlankGroupName() {
        assertThatThrownBy(() ->
                service.createGroup(" ", OWNER_ID)
        ).isInstanceOf(ValidationException.class);

        verifyNoInteractions(
                groupRepository,
                userRepository,
                userGroupRepository
        );
    }

    @Test
    void shouldAddNewUserAsRegularMember() {
        prepareGroup();

        User user = User.builder().id(MEMBER_ID).build();

        when(userRepository.findById(MEMBER_ID))
                .thenReturn(Optional.of(user));

        when(userGroupRepository.save(any(GroupMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GroupMember result = service.addMember(
                GROUP_ID,
                OWNER_ID,
                MEMBER_ID
        );

        assertThat(result.getGroup().getId()).isEqualTo(GROUP_ID);
        assertThat(result.getUser()).isSameAs(user);
        assertThat(result.getRole()).isEqualTo(GroupRole.MEMBER);

        verify(groupPermissionService)
                .requireManager(GROUP_ID, OWNER_ID);
    }

    @Test
    void shouldRejectDuplicateMembership() {
        prepareGroup();

        when(userGroupRepository.existsByGroupIdAndUserId(
                GROUP_ID,
                MEMBER_ID
        )).thenReturn(true);

        assertThatThrownBy(() ->
                service.addMember(GROUP_ID, OWNER_ID, MEMBER_ID)
        ).isInstanceOf(ResourceAlreadyExistsException.class);

        verify(userGroupRepository, never())
                .save(any(GroupMember.class));

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldAllowOwnerToPromoteMemberToAdmin() {
        prepareMembership(GroupRole.MEMBER);

        when(userGroupRepository.save(any(GroupMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GroupMember result = service.changeMemberRole(
                GROUP_ID,
                OWNER_ID,
                MEMBER_ID,
                GroupRole.ADMIN
        );

        assertThat(result.getRole()).isEqualTo(GroupRole.ADMIN);

        verify(groupPermissionService)
                .requireOwner(GROUP_ID, OWNER_ID);
    }

    @Test
    void shouldPreventDemotingOwner() {
        prepareMembership(GroupRole.OWNER);

        assertThatThrownBy(() ->
                service.changeMemberRole(
                        GROUP_ID,
                        OWNER_ID,
                        MEMBER_ID,
                        GroupRole.MEMBER
                )
        ).isInstanceOf(ValidationException.class);

        verify(userGroupRepository, never())
                .save(any(GroupMember.class));
    }

    @Test
    void shouldPreventAssigningOwnershipThroughRoleChange() {
        prepareMembership(GroupRole.MEMBER);

        assertThatThrownBy(() ->
                service.changeMemberRole(
                        GROUP_ID,
                        OWNER_ID,
                        MEMBER_ID,
                        GroupRole.OWNER
                )
        ).isInstanceOf(ValidationException.class);

        verify(userGroupRepository, never())
                .save(any(GroupMember.class));
    }

    @Test
    void shouldRejectMissingRole() {
        prepareGroup();

        assertThatThrownBy(() ->
                service.changeMemberRole(
                        GROUP_ID,
                        OWNER_ID,
                        MEMBER_ID,
                        null
                )
        ).isInstanceOf(ValidationException.class);

        verifyNoInteractions(userGroupRepository);
    }

    private void prepareGroup() {
        when(groupRepository.findById(GROUP_ID))
                .thenReturn(Optional.of(createGroup()));
    }

    private void prepareMembership(GroupRole role) {
        prepareGroup();

        GroupMember member = GroupMember.builder()
                .group(createGroup())
                .user(User.builder().id(MEMBER_ID).build())
                .role(role)
                .build();

        when(userGroupRepository.findByGroupIdAndUserId(
                GROUP_ID,
                MEMBER_ID
        )).thenReturn(Optional.of(member));
    }

    private Group createGroup() {
        return Group.builder()
                .id(GROUP_ID)
                .name("Family")
                .creator(User.builder().id(OWNER_ID).build())
                .build();
    }
}