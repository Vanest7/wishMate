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

import com.gifts.demo.controller.mapper.GroupWebMapperImpl;
import com.gifts.demo.domain.usecase.group.*;

@WebMvcTest(GroupController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class,
        SpringSecurityCurrentUserProvider.class, GroupWebMapperImpl.class})
class GroupControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private CreateGroupUseCase createGroupUseCase;

    @MockitoBean
    private GetMyGroupsUseCase getMyGroupsUseCase;

    @MockitoBean
    private GetGroupUseCase getGroupUseCase;

    @MockitoBean
    private RenameGroupUseCase renameGroupUseCase;

    @MockitoBean
    private DeleteGroupUseCase deleteGroupUseCase;

    private Group group() {
        return Group.builder().id(10).name("Family")
                .creator(User.builder().id(20).build()).build();
    }

    @Test
    void shouldCreateGroupForAuthenticatedUser() throws Exception {
        when(createGroupUseCase.execute("Family", 20)).thenReturn(group());
        mockMvc.perform(post("/api/groups").with(jwt().jwt(t -> t.subject("20")))
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Family\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/groups/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Family"))
                .andExpect(jsonPath("$.creatorId").value(20));
        verify(createGroupUseCase).execute("Family", 20);
    }

    @Test
    void shouldListAuthenticatedUserGroups() throws Exception {
        when(getMyGroupsUseCase.execute(20)).thenReturn(List.of(group()));
        mockMvc.perform(get("/api/groups").with(jwt().jwt(t -> t.subject("20"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].creatorId").value(20));
        verify(getMyGroupsUseCase).execute(20);
    }

    @Test
    void shouldReturnEmptyGroups() throws Exception {
        when(getMyGroupsUseCase.execute(20)).thenReturn(List.of());
        mockMvc.perform(get("/api/groups").with(jwt().jwt(t -> t.subject("20"))))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void shouldGetGroup() throws Exception {
        when(getGroupUseCase.execute(10, 20)).thenReturn(group());
        mockMvc.perform(get("/api/groups/10").with(jwt().jwt(t -> t.subject("20"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Family"));
        verify(getGroupUseCase).execute(10, 20);
    }

    @Test
    void shouldRenameGroup() throws Exception {
        Group renamed = group();
        renamed.setName("Friends");
        when(renameGroupUseCase.execute(10, "Friends", 20)).thenReturn(renamed);
        mockMvc.perform(patch("/api/groups/10").with(jwt().jwt(t -> t.subject("20")))
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Friends\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Friends"));
        verify(renameGroupUseCase).execute(10, "Friends", 20);
    }

    @Test
    void shouldDeleteGroup() throws Exception {
        mockMvc.perform(delete("/api/groups/10").with(jwt().jwt(t -> t.subject("20"))).with(csrf()))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(deleteGroupUseCase).execute(10, 20);
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PATCH"})
    void shouldRejectBlankGroupName(String method) throws Exception {
        String url = method.equals("POST") ? "/api/groups" : "/api/groups/10";
        mockMvc.perform(request(HttpMethod.valueOf(method), url)
                        .with(jwt().jwt(t -> t.subject("20"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
        verifyNoInteractions(createGroupUseCase, renameGroupUseCase);
    }

    @Test
    void shouldRejectOversizedGroupName() throws Exception {
        mockMvc.perform(post("/api/groups").with(jwt().jwt(t -> t.subject("20"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + "a".repeat(101) + "\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.name").exists());
        verifyNoInteractions(createGroupUseCase);
    }

    @Test
    void shouldReturnNotFound() throws Exception {
        when(getGroupUseCase.execute(10, 20))
                .thenThrow(new ResourceNotFoundException("Group not found"));
        mockMvc.perform(get("/api/groups/10").with(jwt().jwt(t -> t.subject("20"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturnForbiddenFromUseCase() throws Exception {
        doThrow(new ForbiddenOperationException("Access denied"))
                .when(deleteGroupUseCase).execute(10, 20);
        mockMvc.perform(delete("/api/groups/10").with(jwt().jwt(t -> t.subject("20"))).with(csrf()))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void shouldRequireAuthenticationForListGroups() throws Exception {
        mockMvc.perform(request(HttpMethod.GET, "/api/groups").with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(createGroupUseCase, getMyGroupsUseCase, getGroupUseCase, renameGroupUseCase, deleteGroupUseCase);
    }

    @Test
    void shouldRequireAuthenticationForGetGroup() throws Exception {
        mockMvc.perform(request(HttpMethod.GET, "/api/groups/10").with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(createGroupUseCase, getMyGroupsUseCase, getGroupUseCase, renameGroupUseCase, deleteGroupUseCase);
    }

    @Test
    void shouldRequireAuthenticationForCreateGroup() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/groups").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Family\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(createGroupUseCase, getMyGroupsUseCase, getGroupUseCase, renameGroupUseCase, deleteGroupUseCase);
    }

    @Test
    void shouldRequireCsrfForCreateGroup() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/groups").with(authenticatedUserWithoutCsrfBypass()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Family\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(createGroupUseCase, getMyGroupsUseCase, getGroupUseCase, renameGroupUseCase, deleteGroupUseCase);
    }

    @Test
    void shouldRequireAuthenticationForRenameGroup() throws Exception {
        mockMvc.perform(request(HttpMethod.PATCH, "/api/groups/10").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Family\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(createGroupUseCase, getMyGroupsUseCase, getGroupUseCase, renameGroupUseCase, deleteGroupUseCase);
    }

    @Test
    void shouldRequireCsrfForRenameGroup() throws Exception {
        mockMvc.perform(request(HttpMethod.PATCH, "/api/groups/10").with(authenticatedUserWithoutCsrfBypass()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Family\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(createGroupUseCase, getMyGroupsUseCase, getGroupUseCase, renameGroupUseCase, deleteGroupUseCase);
    }

    @Test
    void shouldRequireAuthenticationForDeleteGroup() throws Exception {
        mockMvc.perform(request(HttpMethod.DELETE, "/api/groups/10").with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(createGroupUseCase, getMyGroupsUseCase, getGroupUseCase, renameGroupUseCase, deleteGroupUseCase);
    }

    @Test
    void shouldRequireCsrfForDeleteGroup() throws Exception {
        mockMvc.perform(request(HttpMethod.DELETE, "/api/groups/10").with(authenticatedUserWithoutCsrfBypass()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(createGroupUseCase, getMyGroupsUseCase, getGroupUseCase, renameGroupUseCase, deleteGroupUseCase);
    }

    private RequestPostProcessor authenticatedUserWithoutCsrfBypass() {
        Jwt token = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("20")
                .build();

        return authentication(new JwtAuthenticationToken(token));
    }
}
