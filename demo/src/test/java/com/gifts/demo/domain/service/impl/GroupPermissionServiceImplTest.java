package com.gifts.demo.domain.service.impl;

import com.gifts.demo.common.exception.ForbiddenOperationException;
import com.gifts.demo.domain.model.GroupRole;
import com.gifts.demo.domain.repository.UserGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupPermissionServiceImplTest {

    private static final Integer GROUP_ID = 1;
    private static final Integer USER_ID = 20;

    @Mock
    private UserGroupRepository userGroupRepository;

    private GroupPermissionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GroupPermissionServiceImpl(userGroupRepository);
    }

    @Test
    void shouldAllowExistingMember() {
        when(userGroupRepository.existsByGroupIdAndUserId(
                GROUP_ID,
                USER_ID
        )).thenReturn(true);

        assertThatCode(() ->
                service.requireMembership(GROUP_ID, USER_ID)
        ).doesNotThrowAnyException();

        verify(userGroupRepository).existsByGroupIdAndUserId(
                GROUP_ID,
                USER_ID
        );
    }

    @Test
    void shouldRejectUserOutsideGroup() {
        when(userGroupRepository.existsByGroupIdAndUserId(
                GROUP_ID,
                USER_ID
        )).thenReturn(false);

        assertThatThrownBy(() ->
                service.requireMembership(GROUP_ID, USER_ID)
        ).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void shouldAllowManagementWhenUserHasOwnerOrAdminRole() {
        List<GroupRole> allowedRoles = List.of(
                GroupRole.OWNER,
                GroupRole.ADMIN
        );

        when(userGroupRepository.hasAnyRole(
                GROUP_ID,
                USER_ID,
                allowedRoles
        )).thenReturn(true);

        assertThatCode(() ->
                service.requireManager(GROUP_ID, USER_ID)
        ).doesNotThrowAnyException();

        verify(userGroupRepository).hasAnyRole(
                GROUP_ID,
                USER_ID,
                allowedRoles
        );
    }

    @Test
    void shouldRejectManagementWithoutOwnerOrAdminRole() {
        when(userGroupRepository.hasAnyRole(
                GROUP_ID,
                USER_ID,
                List.of(GroupRole.OWNER, GroupRole.ADMIN)
        )).thenReturn(false);

        assertThatThrownBy(() ->
                service.requireManager(GROUP_ID, USER_ID)
        ).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void shouldAllowOwnerOnlyOperationForOwner() {
        when(userGroupRepository.hasRole(
                GROUP_ID,
                USER_ID,
                GroupRole.OWNER
        )).thenReturn(true);

        assertThatCode(() ->
                service.requireOwner(GROUP_ID, USER_ID)
        ).doesNotThrowAnyException();

        verify(userGroupRepository).hasRole(
                GROUP_ID,
                USER_ID,
                GroupRole.OWNER
        );
    }

    @Test
    void shouldRejectOwnerOnlyOperationWithoutOwnerRole() {
        when(userGroupRepository.hasRole(
                GROUP_ID,
                USER_ID,
                GroupRole.OWNER
        )).thenReturn(false);

        assertThatThrownBy(() ->
                service.requireOwner(GROUP_ID, USER_ID)
        ).isInstanceOf(ForbiddenOperationException.class);
    }
}