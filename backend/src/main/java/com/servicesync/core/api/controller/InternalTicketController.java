package com.servicesync.core.api.controller;

import com.servicesync.core.service.TicketService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/internal/v1/tickets")
public class InternalTicketController {

    private final TicketService ticketService;

    public InternalTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/breaches")
    public List<Long> getSlaBreachedTicketIds(@RequestParam("threshold") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime threshold) {
        return ticketService.getSlaBreachedTicketIds(threshold);
    }
}
