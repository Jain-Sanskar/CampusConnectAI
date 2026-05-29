package com.campusconnect.controller;

import com.campusconnect.dto.ResourceDto;
import com.campusconnect.entity.User;
import com.campusconnect.exception.GlobalExceptionHandler;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.ResourceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ResourceControllerTest {

    @Mock
    private ResourceService resourceService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ResourceController resourceController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(resourceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private ResourceDto dto(Long id) {
        ResourceDto d = new ResourceDto();
        d.setId(id);
        d.setTitle("DBMS PYQ");
        d.setCategory("Previous Year Questions");
        d.setSubject("DBMS");
        d.setResourceUrl("https://drive.google.com/dbms");
        d.setType("PDF");
        return d;
    }

    @Test
    void listReturnsResources() throws Exception {
        when(resourceService.list(null, null)).thenReturn(List.of(dto(1L)));

        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subject").value("DBMS"));
    }

    @Test
    void getByIdReturnsNotFoundWhenMissing() throws Exception {
        when(resourceService.getById(99L)).thenThrow(new ResourceNotFoundException("Resource not found with id: 99"));

        mockMvc.perform(get("/api/resources/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createReturnsCreatedForValidBody() throws Exception {
        authenticateAsAdmin();
        when(userRepository.findByEmail("adminA@test.com")).thenReturn(Optional.of(adminUser()));
        when(resourceService.create(any(ResourceDto.class), eq(7L))).thenReturn(dto(10L));

        mockMvc.perform(post("/api/resources")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto(null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void createRejectsInvalidBody() throws Exception {
        authenticateAsAdmin();
        ResourceDto invalid = new ResourceDto(); // missing required title/category/subject/url

        mockMvc.perform(post("/api/resources")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/resources/1"))
                .andExpect(status().isNoContent());
    }

    private User adminUser() {
        User u = new User("Admin A", "adminA@test.com", "hashed", com.campusconnect.entity.Role.ADMIN);
        u.setId(7L);
        return u;
    }

    private void authenticateAsAdmin() {
        UserDetails principal = org.springframework.security.core.userdetails.User.builder()
                .username("adminA@test.com").password("hashed").authorities("ROLE_ADMIN").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null,
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")));
    }
}
