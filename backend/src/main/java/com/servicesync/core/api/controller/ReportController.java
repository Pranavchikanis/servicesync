package com.servicesync.core.api.controller;

import com.servicesync.core.api.dto.TicketDTO;
import com.servicesync.core.api.mapper.DtoMapper;
import com.servicesync.core.service.TicketService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final TicketService ticketService;

    public ReportController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/sla-breaches")
    public List<TicketDTO> getSlaBreaches(@RequestParam(value = "days", defaultValue = "30") int days) {
        return ticketService.getSlaBreaches(days).stream()
                .map(DtoMapper::toTicketDTO)
                .toList();
    }
}
