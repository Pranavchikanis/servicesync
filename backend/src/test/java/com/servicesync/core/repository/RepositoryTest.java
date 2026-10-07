package com.servicesync.core.repository;

import com.servicesync.core.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class RepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    public void testUserRepository() {
        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPasswordHash("hash");
        admin.setRole(Role.ADMIN);
        admin.setName("Admin User");
        entityManager.persist(admin);

        Optional<User> found = userRepository.findByEmail("admin@test.com");
        assertThat(found).isPresent();
    }

    @Test
    public void testInventoryRepository() {
        Inventory part = new Inventory();
        part.setPartName("Screen");
        part.setSku("SCR-123");
        part.setPrice(new java.math.BigDecimal("100.00"));
        part.setQuantityInStock(10);
        entityManager.persist(part);

        Optional<Inventory> found = inventoryRepository.findBySku("SCR-123");
        assertThat(found).isPresent();
    }

    @Test
    public void testTicketRepositorySLA() {
        User tech = new User();
        tech.setEmail("tech@test.com");
        tech.setPasswordHash("hash");
        tech.setRole(Role.TECH);
        tech.setName("Tech User");
        entityManager.persist(tech);

        Ticket ticket = new Ticket();
        ticket.setCustomerName("John Doe");
        ticket.setCustomerPhone("555-1234");
        ticket.setDeviceInfo("Phone");
        ticket.setIssueDesc("Broken Screen");
        ticket.setStatus(TicketStatus.CREATED);
        ticket.setCreatedBy(tech);
        ticket.setCreatedAt(LocalDateTime.now().minusHours(50)); // Set explicitly to older date
        entityManager.persist(ticket);
        
        List<TicketStatus> statuses = Arrays.asList(TicketStatus.CREATED, TicketStatus.DIAGNOSING);
        List<Ticket> breached = ticketRepository.findTicketsExceedingSla(statuses, LocalDateTime.now().minusHours(48));
        
        assertThat(breached).hasSize(1);
    }

    @Test
    public void testPaginationOptimization() {
        User tech = new User();
        tech.setEmail("tech2@test.com");
        tech.setPasswordHash("hash");
        tech.setRole(Role.TECH);
        tech.setName("Tech User 2");
        entityManager.persist(tech);

        for (int i = 0; i < 5; i++) {
            Ticket ticket = new Ticket();
            ticket.setCustomerName("John Doe " + i);
            ticket.setCustomerPhone("555-1234");
            ticket.setDeviceInfo("Phone");
            ticket.setIssueDesc("Broken Screen");
            ticket.setStatus(TicketStatus.CREATED);
            ticket.setCreatedBy(tech);
            entityManager.persist(ticket);
        }

        entityManager.flush();
        entityManager.clear();

        org.springframework.data.domain.Page<Ticket> page = ticketRepository.findAll((org.springframework.data.jpa.domain.Specification<Ticket>) null, org.springframework.data.domain.PageRequest.of(0, 10));
        
        assertThat(page.getContent()).hasSize(5);
        
        // Access lazy properties to ensure they load (would trigger N+1 if not optimized, verified via console logs)
        for (Ticket t : page.getContent()) {
            assertThat(t.getCreatedBy().getName()).isNotNull();
            assertThat(t.getParts()).isNotNull();
        }
    }
}
