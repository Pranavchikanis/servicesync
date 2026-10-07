package com.servicesync.core.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.servicesync.core.security.CustomUserDetailsService;
import com.servicesync.core.security.JwtAuthenticationFilter;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.servicesync.core.security.SecurityConfig;
import com.servicesync.core.security.JwtUtil;

@WebMvcTest(controllers = ExceptionTestController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void testValidationFailure() throws Exception {
        mockMvc.perform(post("/api/test-exceptions/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0].field").value("requiredField"))
                .andExpect(jsonPath("$.details[0].issue").value("Field cannot be blank"));
    }

    @Test
    void testNotFound() throws Exception {
        mockMvc.perform(get("/api/test-exceptions/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Not found test"));
    }

    @Test
    void testForbidden() throws Exception {
        mockMvc.perform(get("/api/test-exceptions/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Forbidden test"));
    }

    @Test
    void testConflictState() throws Exception {
        mockMvc.perform(get("/api/test-exceptions/conflict-state"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INVALID_STATE_TRANSITION"))
                .andExpect(jsonPath("$.message").value("Bad state"));
    }

    @Test
    void testConflictStock() throws Exception {
        mockMvc.perform(get("/api/test-exceptions/conflict-stock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.message").value("Not enough stock"));
    }

    @Test
    void testConflictLock() throws Exception {
        mockMvc.perform(get("/api/test-exceptions/conflict-lock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONCURRENT_MODIFICATION"));
    }

    @Test
    void testConflictData() throws Exception {
        mockMvc.perform(get("/api/test-exceptions/conflict-data"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DATA_INTEGRITY_VIOLATION"));
    }

    @Test
    void testInternalError() throws Exception {
        mockMvc.perform(get("/api/test-exceptions/internal"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
