package com.servicesync.core.service;

import com.servicesync.core.domain.*;
import com.servicesync.core.event.TicketStatusChangedEvent;
import com.servicesync.core.exception.*;
import com.servicesync.core.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final TicketHistoryRepository historyRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository,
                         InventoryRepository inventoryRepository, TicketHistoryRepository historyRepository, ApplicationEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.inventoryRepository = inventoryRepository;
        this.historyRepository = historyRepository;
        this.eventPublisher = eventPublisher;
    }

    public Ticket createTicket(String customerName, String customerPhone, String deviceInfo, String issueDesc, Long createdById) {
        if (customerName == null || customerPhone == null || deviceInfo == null || issueDesc == null) {
            throw new IllegalArgumentException("Customer details and device info are required");
        }
        
        User creator = getUser(createdById);

        Ticket ticket = new Ticket();
        ticket.setCustomerName(customerName);
        ticket.setCustomerPhone(customerPhone);
        ticket.setDeviceInfo(deviceInfo);
        ticket.setIssueDesc(issueDesc);
        ticket.setStatus(TicketStatus.CREATED);
        ticket.setCreatedBy(creator);
        
        return ticketRepository.save(ticket);
    }

    public Ticket assignTechnician(Long ticketId, Long technicianId, Long requesterId) {
        Ticket ticket = getTicket(ticketId);
        User requester = getUser(requesterId);
        User technician = getUser(technicianId);

        if (ticket.getStatus() != TicketStatus.CREATED && requester.getRole() != Role.ADMIN) {
            if (ticket.getTechnician() != null && !ticket.getTechnician().getId().equals(technicianId)) {
                throw new UnauthorizedActionException("Only admins can reassign technicians for tickets in progress");
            }
        }

        ticket.setTechnician(technician);
        return ticketRepository.save(ticket);
    }

    public Ticket startDiagnosis(Long ticketId, Long technicianId, String notes) {
        Ticket ticket = getTicket(ticketId);
        
        if (ticket.getStatus() != TicketStatus.CREATED) {
            throw new InvalidTicketStateException("Cannot start diagnosis from state: " + ticket.getStatus());
        }
        
        enforceTechnicianAssignment(ticket, technicianId);

        changeStatus(ticket, TicketStatus.DIAGNOSING, technicianId, notes);
        return ticketRepository.save(ticket);
    }
    
    public Ticket addPart(Long ticketId, Long inventoryId, int quantity, Long technicianId) {
        Ticket ticket = getTicket(ticketId);
        enforceTechnicianAssignment(ticket, technicianId);
        
        if (ticket.getStatus() == TicketStatus.CLOSED || ticket.getStatus() == TicketStatus.RESOLVED) {
            throw new InvalidTicketStateException("Cannot add parts to a resolved/closed ticket");
        }
        
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory part not found"));
                
        if (inventory.getQuantityInStock() < quantity) {
            throw new InsufficientInventoryException("Not enough stock for part: " + inventory.getPartName());
        }
        
        inventory.setQuantityInStock(inventory.getQuantityInStock() - quantity);
        inventoryRepository.save(inventory);
        
        TicketPart part = new TicketPart();
        part.setInventory(inventory);
        part.setQuantityUsed(quantity);
        part.setPriceAtTime(inventory.getPrice());
        
        ticket.addPart(part);
        ticketRepository.save(ticket);
        
        if (ticket.getStatus() == TicketStatus.DIAGNOSING || ticket.getStatus() == TicketStatus.WAITING_PARTS) {
             changeStatus(ticket, TicketStatus.IN_REPAIR, technicianId, null);
        }
        
        return ticket;
    }
    
    public Ticket startRepair(Long ticketId, Long technicianId, String notes) {
        Ticket ticket = getTicket(ticketId);
        enforceTechnicianAssignment(ticket, technicianId);
        
        if (ticket.getStatus() != TicketStatus.DIAGNOSING && ticket.getStatus() != TicketStatus.WAITING_PARTS) {
            throw new InvalidTicketStateException("Cannot start repair from " + ticket.getStatus());
        }
        
        changeStatus(ticket, TicketStatus.IN_REPAIR, technicianId, notes);
        return ticketRepository.save(ticket);
    }

    public Ticket markWaitingParts(Long ticketId, Long technicianId, String notes) {
        Ticket ticket = getTicket(ticketId);
        enforceTechnicianAssignment(ticket, technicianId);
        
        if (ticket.getStatus() != TicketStatus.DIAGNOSING) {
            throw new InvalidTicketStateException("Can only wait for parts from DIAGNOSING state");
        }
        
        changeStatus(ticket, TicketStatus.WAITING_PARTS, technicianId, notes);
        return ticketRepository.save(ticket);
    }
    
    public Ticket resolveTicket(Long ticketId, Long technicianId, String notes) {
        Ticket ticket = getTicket(ticketId);
        enforceTechnicianAssignment(ticket, technicianId);
        
        if (ticket.getStatus() != TicketStatus.IN_REPAIR) {
             throw new InvalidTicketStateException("Must be IN_REPAIR to resolve");
        }
        
        BigDecimal totalCost = BigDecimal.ZERO;
        for (TicketPart part : ticket.getParts()) {
            BigDecimal lineTotal = part.getPriceAtTime().multiply(new BigDecimal(part.getQuantityUsed()));
            totalCost = totalCost.add(lineTotal);
        }
        totalCost = totalCost.add(new BigDecimal("50.00")); // labor fee
        
        ticket.setTotalCost(totalCost);
        ticket.setResolvedAt(LocalDateTime.now());
        
        changeStatus(ticket, TicketStatus.RESOLVED, technicianId, notes);
        return ticketRepository.save(ticket);
    }
    
    public Ticket closeTicket(Long ticketId, Long adminId, String notes) {
        Ticket ticket = getTicket(ticketId);
        
        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new InvalidTicketStateException("Must be RESOLVED to close");
        }
        
        changeStatus(ticket, TicketStatus.CLOSED, adminId, notes);
        return ticketRepository.save(ticket);
    }

    private void changeStatus(Ticket ticket, TicketStatus newStatus, Long changedById, String notes) {
        String oldStatus = ticket.getStatus().name();
        ticket.setStatus(newStatus);
        eventPublisher.publishEvent(new TicketStatusChangedEvent(ticket, oldStatus, newStatus.name(), changedById, notes));
    }

    private void enforceTechnicianAssignment(Ticket ticket, Long requesterId) {
        User requester = getUser(requesterId);
        if (requester.getRole() == Role.ADMIN) {
            return;
        }
        if (ticket.getTechnician() == null || !ticket.getTechnician().getId().equals(requesterId)) {
            throw new UnauthorizedActionException("Only the assigned technician can perform this action");
        }
    }

    @Transactional(readOnly = true)
    public Ticket getTicketById(Long id) {
        return ticketRepository.findWithPartsById(id).orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }
    
    @Transactional(readOnly = true)
    public Ticket getTicketByIdAndPhone(Long id, String phone) {
        return ticketRepository.findByIdAndCustomerPhone(id, phone).orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }

    @Transactional(readOnly = true)
    public Page<Ticket> getPaginatedTickets(Pageable pageable, TicketStatus status, Long technicianId) {
        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (technicianId != null) {
                predicates.add(cb.equal(root.get("technician").get("id"), technicianId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return ticketRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<Ticket> getSlaBreaches(int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        List<TicketStatus> activeStatuses = List.of(TicketStatus.CREATED, TicketStatus.DIAGNOSING, TicketStatus.WAITING_PARTS, TicketStatus.IN_REPAIR);
        return ticketRepository.findTicketsExceedingSla(activeStatuses, threshold);
    }
    
    @Transactional(readOnly = true)
    public List<Long> getSlaBreachedTicketIds(LocalDateTime threshold) {
        List<TicketStatus> activeStatuses = List.of(TicketStatus.CREATED, TicketStatus.DIAGNOSING);
        return ticketRepository.findTicketIdsExceedingSla(activeStatuses, threshold);
    }
    
    @Transactional(readOnly = true)
    public List<TicketHistory> getTicketHistory(Long ticketId) {
        return historyRepository.findByTicketId(ticketId);
    }

    private Ticket getTicket(Long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
