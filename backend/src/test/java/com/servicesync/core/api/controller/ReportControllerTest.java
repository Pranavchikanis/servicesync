package com.servicesync.core.api.controller;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.servicesync.core.domain.Ticket;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
public class ReportControllerTest {

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
    void testGetSlaBreaches() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setId(101L);
        ticket.setCustomerName("John Doe");

        when(ticketService.getSlaBreaches(anyInt())).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/v1/reports/sla-breaches")
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(101));
    }
}
