package com.servicesync.core.api.controller;

import com.servicesync.core.domain.Role;
import com.servicesync.core.domain.User;
import com.servicesync.core.security.CustomUserDetailsService;
import com.servicesync.core.security.JwtAuthenticationFilter;
import com.servicesync.core.security.JwtUtil;
import com.servicesync.core.security.SecurityConfig;
import com.servicesync.core.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetTechniciansAsAdmin() throws Exception {
        User tech = new User();
        tech.setId(2L);
        tech.setName("Tech Bob");
        tech.setEmail("tech@example.com");
        tech.setRole(Role.TECH);

        when(userService.getTechnicians()).thenReturn(List.of(tech));

        mockMvc.perform(get("/api/v1/users/technicians"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Tech Bob"));
    }

    @Test
    @WithMockUser(roles = "TECH")
    void testGetTechniciansAsTech() throws Exception {
        mockMvc.perform(get("/api/v1/users/technicians"))
                .andExpect(status().isForbidden());
    }
}
