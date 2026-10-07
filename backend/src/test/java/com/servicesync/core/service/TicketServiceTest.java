package com.servicesync.core.service;

import com.servicesync.core.domain.*;
import com.servicesync.core.event.TicketStatusChangedEvent;
import com.servicesync.core.exception.*;
import com.servicesync.core.repository.TicketRepository;
import com.servicesync.core.repository.UserRepository;
import com.servicesync.core.repository.InventoryRepository;
import com.servicesync.core.repository.TicketHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock
    private UserRepository userRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private TicketHistoryRepository historyRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TicketService ticketService;

    private User admin;
    private User tech;
    private Ticket ticket;
    private Inventory part;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(ticketRepository, userRepository, inventoryRepository, historyRepository, eventPublisher);
        admin = new User();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        tech = new User();
        tech.setId(2L);
        tech.setRole(Role.TECH);

        ticket = new Ticket();
        ticket.setId(100L);
        ticket.setStatus(TicketStatus.CREATED);
        ticket.setParts(new ArrayList<>());

        part = new Inventory();
        part.setId(200L);
        part.setQuantityInStock(10);
        part.setPrice(new BigDecimal("10.00"));
        part.setPartName("Battery");
    }

    @Test
    void testCreateTicket() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        Ticket created = ticketService.createTicket("John", "555", "Phone", "Broken", 1L);
        
        assertThat(created.getStatus()).isEqualTo(TicketStatus.CREATED);
        assertThat(created.getCustomerName()).isEqualTo("John");
    }

    @Test
    void testAssignTechnician() {
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(tech));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        ticketService.assignTechnician(100L, 2L, 1L);
        assertThat(ticket.getTechnician()).isEqualTo(tech);
    }
    
    @Test
    void testReassignTechnicianFailsForNonAdmin() {
        ticket.setStatus(TicketStatus.DIAGNOSING);
        ticket.setTechnician(tech); // Tech is 2L
        
        User anotherTech = new User();
        anotherTech.setId(3L);
        anotherTech.setRole(Role.TECH);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(3L)).thenReturn(Optional.of(anotherTech));
        
        // tech 3L tries to reassign ticket already in progress
        assertThatThrownBy(() -> ticketService.assignTechnician(100L, 3L, 3L))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void testInvalidTransitionThrowsException() {
        ticket.setTechnician(tech);
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(tech));
        
        // Cannot wait for parts from CREATED
        assertThatThrownBy(() -> ticketService.markWaitingParts(100L, 2L, null))
                .isInstanceOf(InvalidTicketStateException.class);
    }

    @Test
    void testAddPartSuccess() {
        ticket.setStatus(TicketStatus.DIAGNOSING);
        ticket.setTechnician(tech);
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(tech));
        when(inventoryRepository.findById(200L)).thenReturn(Optional.of(part));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        ticketService.addPart(100L, 200L, 2, 2L);
        
        assertThat(part.getQuantityInStock()).isEqualTo(8);
        assertThat(ticket.getParts()).hasSize(1);
        assertThat(ticket.getParts().get(0).getQuantityUsed()).isEqualTo(2);
        
        // Should transition to IN_REPAIR
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_REPAIR);
        
        verify(eventPublisher).publishEvent(any(TicketStatusChangedEvent.class));
    }

    @Test
    void testAddPartInsufficientStock() {
        ticket.setStatus(TicketStatus.DIAGNOSING);
        ticket.setTechnician(tech);
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(tech));
        when(inventoryRepository.findById(200L)).thenReturn(Optional.of(part));

        assertThatThrownBy(() -> ticketService.addPart(100L, 200L, 11, 2L))
                .isInstanceOf(InsufficientInventoryException.class);
    }
}
