package com.servicesync.core.api.controller;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.servicesync.core.domain.Role;
import com.servicesync.core.domain.Ticket;
import com.servicesync.core.domain.TicketStatus;
import com.servicesync.core.domain.User;
import com.servicesync.core.security.CustomUserDetailsService;
import com.servicesync.core.security.JwtAuthenticationFilter;
import com.servicesync.core.security.JwtUtil;
import com.servicesync.core.security.SecurityConfig;
import com.servicesync.core.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
public class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        Claim roleClaim = mock(Claim.class);
        Claim userIdClaim = mock(Claim.class);

        when(roleClaim.asString()).thenReturn("ADMIN");
        when(userIdClaim.asLong()).thenReturn(1L);

        when(decodedJWT.getSubject()).thenReturn("admin@example.com");
        when(decodedJWT.getClaim("role")).thenReturn(roleClaim);
        when(decodedJWT.getClaim("userId")).thenReturn(userIdClaim);

        when(jwtUtil.validateTokenAndGetDecodedJWT("mock-token")).thenReturn(decodedJWT);
    }

    @Test
    void testCreateTicket() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        Ticket ticket = new Ticket();
        ticket.setId(101L);
        ticket.setCustomerName("John Doe");
        ticket.setStatus(TicketStatus.CREATED);
        ticket.setCreatedBy(admin);

        when(ticketService.createTicket(anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(ticket);

        String payload = """
                {
                  "customerName": "John Doe",
                  "customerPhone": "1234567890",
                  "deviceInfo": "iPhone 13",
                  "issueDesc": "Broken screen"
                }
                """;

        mockMvc.perform(post("/api/v1/tickets")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }
    
    @Test
    void testGetTicketUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/101"))
                .andExpect(status().isUnauthorized());
    }
}
