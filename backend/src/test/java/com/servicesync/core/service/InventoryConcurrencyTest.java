package com.servicesync.core.service;

import com.servicesync.core.domain.Inventory;
import com.servicesync.core.domain.Role;
import com.servicesync.core.domain.Ticket;
import com.servicesync.core.domain.TicketStatus;
import com.servicesync.core.domain.User;
import com.servicesync.core.repository.InventoryRepository;
import com.servicesync.core.repository.TicketRepository;
import com.servicesync.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class InventoryConcurrencyTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    private Long inventoryId;
    private Long ticketId1;
    private Long ticketId2;
    private Long techId;

    @BeforeEach
    public void setup() {
        ticketRepository.deleteAll();
        inventoryRepository.deleteAll();
        userRepository.deleteAll();

        User tech = new User();
        tech.setName("Tech");
        tech.setEmail("tech@concurrency.com");
        tech.setPasswordHash("hash");
        tech.setRole(Role.TECH);
        tech = userRepository.save(tech);
        techId = tech.getId();

        Inventory part = new Inventory();
        part.setSku("CONC-PART");
        part.setPartName("Concurrency Part");
        part.setPrice(BigDecimal.TEN);
        part.setQuantityInStock(1);
        part = inventoryRepository.save(part);
        inventoryId = part.getId();

        Ticket t1 = new Ticket();
        t1.setCustomerName("Customer A");
        t1.setCustomerPhone("123");
        t1.setDeviceInfo("Device A");
        t1.setIssueDesc("Issue A");
        t1.setStatus(TicketStatus.DIAGNOSING);
        t1.setCreatedBy(tech);
        t1.setTechnician(tech);
        t1 = ticketRepository.save(t1);
        ticketId1 = t1.getId();

        Ticket t2 = new Ticket();
        t2.setCustomerName("Customer B");
        t2.setCustomerPhone("456");
        t2.setDeviceInfo("Device B");
        t2.setIssueDesc("Issue B");
        t2.setStatus(TicketStatus.DIAGNOSING);
        t2.setCreatedBy(tech);
        t2.setTechnician(tech);
        t2 = ticketRepository.save(t2);
        ticketId2 = t2.getId();
    }

    @Test
    public void testOptimisticLockingOnInventory() throws Exception {
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            ticketService.addPart(ticketId1, inventoryId, 1, techId);
        });

        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            ticketService.addPart(ticketId2, inventoryId, 1, techId);
        });

        boolean optimisticLockExceptionThrown = false;
        try {
            CompletableFuture.allOf(future1, future2).get();
        } catch (ExecutionException e) {
            Throwable rootCause = e.getCause();
            if (rootCause instanceof ObjectOptimisticLockingFailureException) {
                optimisticLockExceptionThrown = true;
            } else if (rootCause.getCause() instanceof ObjectOptimisticLockingFailureException) {
                optimisticLockExceptionThrown = true;
            } else if (rootCause.getClass().getSimpleName().contains("ObjectOptimisticLockingFailureException")) {
                optimisticLockExceptionThrown = true;
            } else {
                throw new RuntimeException("Unexpected exception: " + rootCause);
            }
        }

        assertTrue(optimisticLockExceptionThrown, "An ObjectOptimisticLockingFailureException should have been thrown due to concurrent modification");

        Inventory updatedPart = inventoryRepository.findById(inventoryId).orElseThrow();
        assertThat(updatedPart.getQuantityInStock()).isEqualTo(0);
    }
}
