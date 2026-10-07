package com.servicesync.core.api.controller;

import com.servicesync.core.domain.Ticket;
import com.servicesync.core.domain.TicketStatus;
import com.servicesync.core.exception.ResourceNotFoundException;
import com.servicesync.core.security.CustomUserDetailsService;
import com.servicesync.core.security.JwtAuthenticationFilter;
import com.servicesync.core.security.JwtUtil;
import com.servicesync.core.security.SecurityConfig;
import com.servicesync.core.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicTicketController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
public class PublicTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void testGetPublicTicketFound() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setId(101L);
        ticket.setStatus(TicketStatus.IN_REPAIR);
        ticket.setDeviceInfo("iPhone 13");

        when(ticketService.getTicketByIdAndPhone(101L, "1234")).thenReturn(ticket);

        mockMvc.perform(get("/api/v1/public/tickets/101")
                .param("phone", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.status").value("IN_REPAIR"))
                .andExpect(jsonPath("$.deviceInfo").value("iPhone 13"));
    }

    @Test
    void testGetPublicTicketNotFound() throws Exception {
        when(ticketService.getTicketByIdAndPhone(anyLong(), anyString()))
                .thenThrow(new ResourceNotFoundException("Ticket not found"));

        mockMvc.perform(get("/api/v1/public/tickets/999")
                .param("phone", "0000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetPublicTicketMissingPhone() throws Exception {
        mockMvc.perform(get("/api/v1/public/tickets/101"))
                .andExpect(status().isBadRequest());
    }
}
