package com.servicesync.core.api.controller;

import com.servicesync.core.api.dto.TicketDTO;
import com.servicesync.core.api.mapper.DtoMapper;
import com.servicesync.core.service.TicketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/tickets")
public class PublicTicketController {

    private final TicketService ticketService;

    public PublicTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/{ticketId}")
    public TicketDTO getPublicTicket(@PathVariable("ticketId") Long ticketId, @RequestParam("phone") String phone) {
        return DtoMapper.toTicketDTO(ticketService.getTicketByIdAndPhone(ticketId, phone));
    }
}
