package com.servicesync.core.api.controller;

import com.servicesync.core.api.dto.*;
import com.servicesync.core.api.mapper.DtoMapper;
import com.servicesync.core.domain.TicketStatus;
import com.servicesync.core.domain.Role;
import com.servicesync.core.service.TicketService;
import com.servicesync.core.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@PreAuthorize("hasAnyRole('ADMIN', 'TECH')")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDTO createTicket(@Valid @RequestBody TicketCreateRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return DtoMapper.toTicketDTO(ticketService.createTicket(
                request.getCustomerName(),
                request.getCustomerPhone(),
                request.getDeviceInfo(),
                request.getIssueDesc(),
                userDetails.getUser().getId()
        ));
    }

    @GetMapping
    public Page<TicketDTO> listTickets(
            @RequestParam(value = "status", required = false) TicketStatus status,
            @RequestParam(value = "technicianId", required = false) Long technicianId,
            Pageable pageable) {
        return ticketService.getPaginatedTickets(pageable, status, technicianId)
                .map(DtoMapper::toTicketDTO);
    }

    @GetMapping("/{ticketId}")
    public TicketDTO getTicket(@PathVariable("ticketId") Long ticketId) {
        return DtoMapper.toTicketDTO(ticketService.getTicketById(ticketId));
    }

    @PatchMapping("/{ticketId}/status")
    public TicketDTO updateTicketStatus(
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody TicketStatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        TicketStatus newStatus = TicketStatus.valueOf(request.getStatus().toUpperCase());
        Long userId = userDetails.getUser().getId();
        
        String notes = request.getNotes();
        
        return switch (newStatus) {
            case DIAGNOSING -> DtoMapper.toTicketDTO(ticketService.startDiagnosis(ticketId, userId, notes));
            case WAITING_PARTS -> DtoMapper.toTicketDTO(ticketService.markWaitingParts(ticketId, userId, notes));
            case IN_REPAIR -> DtoMapper.toTicketDTO(ticketService.startRepair(ticketId, userId, notes));
            case RESOLVED -> DtoMapper.toTicketDTO(ticketService.resolveTicket(ticketId, userId, notes));
            case CLOSED -> {
                // Must be admin
                if (userDetails.getUser().getRole() != Role.ADMIN) {
                    throw new com.servicesync.core.exception.UnauthorizedActionException("Only ADMIN can close tickets");
                }
                yield DtoMapper.toTicketDTO(ticketService.closeTicket(ticketId, userId, notes));
            }
            default -> throw new IllegalArgumentException("Invalid status transition requested");
        };
    }

    @PatchMapping("/{ticketId}/assignment")
    @PreAuthorize("hasRole('ADMIN')")
    public TicketDTO assignTechnician(
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody TicketAssignmentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return DtoMapper.toTicketDTO(ticketService.assignTechnician(ticketId, request.getTechnicianId(), userDetails.getUser().getId()));
    }

    @PostMapping("/{ticketId}/parts")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketPartDTO consumePart(
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody PartConsumptionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var updatedTicket = ticketService.addPart(ticketId, request.getInventoryId(), request.getQuantity(), userDetails.getUser().getId());
        
        // Return the newly added part (the last one in the list for MVP)
        var parts = updatedTicket.getParts();
        return DtoMapper.toTicketPartDTO(parts.get(parts.size() - 1));
    }

    @GetMapping("/{ticketId}/history")
    public List<TicketHistoryDTO> getTicketHistory(@PathVariable("ticketId") Long ticketId) {
        return ticketService.getTicketHistory(ticketId).stream()
                .map(DtoMapper::toTicketHistoryDTO)
                .toList();
    }
}
