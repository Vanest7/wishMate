package com.gifts.demo.controller;

import com.gifts.demo.common.exception.*;
import com.gifts.demo.controller.exception.ApiExceptionHandler;
import com.gifts.demo.domain.model.*;
import com.gifts.demo.security.SecurityConfig;
import com.gifts.demo.security.currentUser.SpringSecurityCurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gifts.demo.controller.mapper.GroupMemberWebMapperImpl;
import com.gifts.demo.domain.usecase.group.*;

@WebMvcTest(GroupMemberController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class,
        SpringSecurityCurrentUserProvider.class, GroupMemberWebMapperImpl.class})
class GroupMemberControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private GetGroupMembersUseCase getGroupMembersUseCase;

    @MockitoBean
    private AddGroupMemberUseCase addGroupMemberUseCase;

    @MockitoBean
    private ChangeGroupMemberRoleUseCase changeGroupMemberRoleUseCase;

    @MockitoBean
    private RemoveGroupMemberUseCase removeGroupMemberUseCase;

    @MockitoBean
    private LeaveGroupUseCase leaveGroupUseCase;

    private GroupMember member(GroupRole role) {
        return GroupMember.builder().group(Group.builder().id(10).build())
                .user(User.builder().id(30).userName("Guest").email("guest@example.test")
                        .passwordHash("private-hash").build())
                .role(role).joinedAt(java.time.LocalDateTime.of(2026, 10, 1, 12, 0)).build();
    }

    @Test
    void shouldListMembersWithoutSensitiveFields() throws Exception {
        when(getGroupMembersUseCase.execute(10, 20)).thenReturn(List.of(member(GroupRole.MEMBER)));
        mockMvc.perform(get("/api/groups/10/members").with(jwt().jwt(t -> t.subject("20"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(30))
                .andExpect(jsonPath("$[0].userName").value("Guest"))
                .andExpect(jsonPath("$[0].role").value("MEMBER"))
                .andExpect(jsonPath("$[0].joinedAt").exists())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$[0].user").doesNotExist());
        verify(getGroupMembersUseCase).execute(10, 20);
    }

    @Test
    void shouldAddMember() throws Exception {
        when(addGroupMemberUseCase.execute(10, 30, 20)).thenReturn(member(GroupRole.MEMBER));
        mockMvc.perform(post("/api/groups/10/members").with(jwt().jwt(t -> t.subject("20")))
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"userId\":30}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.userId").value(30))
                .andExpect(jsonPath("$.role").value("MEMBER"));
        verify(addGroupMemberUseCase).execute(10, 30, 20);
    }

    @Test
    void shouldChangeMemberRole() throws Exception {
        when(changeGroupMemberRoleUseCase.execute(10, 30, GroupRole.ADMIN, 20))
                .thenReturn(member(GroupRole.ADMIN));
        mockMvc.perform(patch("/api/groups/10/members/30").with(jwt().jwt(t -> t.subject("20")))
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN"));
        verify(changeGroupMemberRoleUseCase).execute(10, 30, GroupRole.ADMIN, 20);
    }

    @Test
    void shouldRemoveMember() throws Exception {
        mockMvc.perform(delete("/api/groups/10/members/30")
                        .with(jwt().jwt(t -> t.subject("20"))).with(csrf()))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(removeGroupMemberUseCase).execute(10, 30, 20);
        verifyNoInteractions(leaveGroupUseCase);
    }

    @Test
    void shouldRouteMeToLeaveGroup() throws Exception {
        mockMvc.perform(delete("/api/groups/10/members/me")
                        .with(jwt().jwt(t -> t.subject("20"))).with(csrf()))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(leaveGroupUseCase).execute(10, 20);
        verifyNoInteractions(removeGroupMemberUseCase);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"userId\":0}", "{\"userId\":-1}"})
    void shouldRejectInvalidMemberId(String body) throws Exception {
        mockMvc.perform(post("/api/groups/10/members").with(jwt().jwt(t -> t.subject("20")))
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.userId").exists());
        verifyNoInteractions(addGroupMemberUseCase);
    }

    @Test
    void shouldRejectMissingRole() throws Exception {
        mockMvc.perform(patch("/api/groups/10/members/30").with(jwt().jwt(t -> t.subject("20")))
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.role").exists());
        verifyNoInteractions(changeGroupMemberRoleUseCase);
    }

    @Test
    void shouldRejectUnknownRole() throws Exception {
        mockMvc.perform(patch("/api/groups/10/members/30").with(jwt().jwt(t -> t.subject("20")))
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        verifyNoInteractions(changeGroupMemberRoleUseCase);
    }

    @Test
    void shouldReturnConflictWhenOwnerCannotLeave() throws Exception {
        doThrow(new GroupOperationException(GroupOperationException.Reason.CONFLICT, "The owner cannot leave the group"))
                .when(leaveGroupUseCase).execute(10, 20);
        mockMvc.perform(delete("/api/groups/10/members/me")
                        .with(jwt().jwt(t -> t.subject("20"))).with(csrf()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void shouldReturnForbiddenWhenRoleChangeIsDenied() throws Exception {
        when(changeGroupMemberRoleUseCase.execute(10, 30, GroupRole.ADMIN, 20))
                .thenThrow(new GroupOperationException(GroupOperationException.Reason.FORBIDDEN, "Access denied"));
        mockMvc.perform(patch("/api/groups/10/members/30").with(jwt().jwt(t -> t.subject("20")))
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void shouldRequireAuthenticationForListMembers() throws Exception {
        mockMvc.perform(request(HttpMethod.GET, "/api/groups/10/members").with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(getGroupMembersUseCase, addGroupMemberUseCase, changeGroupMemberRoleUseCase, removeGroupMemberUseCase, leaveGroupUseCase);
    }

    @Test
    void shouldRequireAuthenticationForAddMember() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/groups/10/members").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"userId\":30}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(getGroupMembersUseCase, addGroupMemberUseCase, changeGroupMemberRoleUseCase, removeGroupMemberUseCase, leaveGroupUseCase);
    }

    @Test
    void shouldRequireCsrfForAddMember() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/groups/10/members").with(authenticatedUserWithoutCsrfBypass()).contentType(MediaType.APPLICATION_JSON).content("{\"userId\":30}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(getGroupMembersUseCase, addGroupMemberUseCase, changeGroupMemberRoleUseCase, removeGroupMemberUseCase, leaveGroupUseCase);
    }

    @Test
    void shouldRequireAuthenticationForChangeRole() throws Exception {
        mockMvc.perform(request(HttpMethod.PATCH, "/api/groups/10/members/30").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(getGroupMembersUseCase, addGroupMemberUseCase, changeGroupMemberRoleUseCase, removeGroupMemberUseCase, leaveGroupUseCase);
    }

    @Test
    void shouldRequireCsrfForChangeRole() throws Exception {
        mockMvc.perform(request(HttpMethod.PATCH, "/api/groups/10/members/30").with(authenticatedUserWithoutCsrfBypass()).contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(getGroupMembersUseCase, addGroupMemberUseCase, changeGroupMemberRoleUseCase, removeGroupMemberUseCase, leaveGroupUseCase);
    }

    @Test
    void shouldRequireAuthenticationForRemoveMember() throws Exception {
        mockMvc.perform(request(HttpMethod.DELETE, "/api/groups/10/members/30").with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(getGroupMembersUseCase, addGroupMemberUseCase, changeGroupMemberRoleUseCase, removeGroupMemberUseCase, leaveGroupUseCase);
    }

    @Test
    void shouldRequireCsrfForRemoveMember() throws Exception {
        mockMvc.perform(request(HttpMethod.DELETE, "/api/groups/10/members/30").with(authenticatedUserWithoutCsrfBypass()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(getGroupMembersUseCase, addGroupMemberUseCase, changeGroupMemberRoleUseCase, removeGroupMemberUseCase, leaveGroupUseCase);
    }

    @Test
    void shouldRequireAuthenticationForLeaveGroup() throws Exception {
        mockMvc.perform(request(HttpMethod.DELETE, "/api/groups/10/members/me").with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(getGroupMembersUseCase, addGroupMemberUseCase, changeGroupMemberRoleUseCase, removeGroupMemberUseCase, leaveGroupUseCase);
    }

    @Test
    void shouldRequireCsrfForLeaveGroup() throws Exception {
        mockMvc.perform(request(HttpMethod.DELETE, "/api/groups/10/members/me").with(authenticatedUserWithoutCsrfBypass()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(getGroupMembersUseCase, addGroupMemberUseCase, changeGroupMemberRoleUseCase, removeGroupMemberUseCase, leaveGroupUseCase);
    }

    private RequestPostProcessor authenticatedUserWithoutCsrfBypass() {
        Jwt token = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("20")
                .build();

        return authentication(new JwtAuthenticationToken(token));
    }
}
