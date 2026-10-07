package com.servicesync.core.security;

import com.servicesync.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecurityTestController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void testPublicEndpointIsAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/public/tickets/test"))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpointIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/protected-test-dummy"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testProtectedEndpointAccessibleWithAuth() throws Exception {
        mockMvc.perform(get("/api/v1/protected-test-dummy"))
                .andExpect(status().isOk());
    }
}
